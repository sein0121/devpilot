terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = "ap-northeast-2"
}

# ── 보안그룹: ASG의 backend 인스턴스용 ──
resource "aws_security_group" "asg_backend" {
  name        = "devpilot-asg-backend-sg"
  description = "ASG backend instances"
  vpc_id      = var.vpc_id

  # ALB로부터 8080 인바운드만 허용 (직접 인터넷 노출 없음)
  ingress {
    description     = "From ALB"
    from_port        = 8080
    to_port          = 8080
    protocol         = "tcp"
    security_groups  = [aws_security_group.alb.id]
  }

  # 부하테스트 중 SSH 디버깅용 (기존 EC2와 동일하게 임시 전체 개방, 나중에 제한 권장)
  ingress {
    description = "SSH temp"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "devpilot-asg-backend-sg"
  }
}

# ── 보안그룹: ALB용 ──
resource "aws_security_group" "alb" {
  name        = "devpilot-alb-sg"
  description = "ALB"
  vpc_id      = var.vpc_id

  ingress {
    description = "HTTP from anywhere (CloudFront origin)"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "devpilot-alb-sg"
  }
}

# ── 기존 EC2(MySQL/Redis) 보안그룹에 인바운드 규칙 추가 ──
resource "aws_security_group_rule" "mysql_from_asg" {
  type                     = "ingress"
  from_port                = 3306
  to_port                  = 3306
  protocol                 = "tcp"
  security_group_id        = var.db_host_security_group_id
  source_security_group_id = aws_security_group.asg_backend.id
  description              = "MySQL access from ASG backend"
}

resource "aws_security_group_rule" "redis_from_asg" {
  type                     = "ingress"
  from_port                = 6379
  to_port                  = 6379
  protocol                 = "tcp"
  security_group_id        = var.db_host_security_group_id
  source_security_group_id = aws_security_group.asg_backend.id
  description              = "Redis access from ASG backend"
}

# ── Launch Template ──
resource "aws_launch_template" "backend" {
  name_prefix   = "devpilot-backend-"
  image_id      = data.aws_ssm_parameter.ubuntu_ami.value
  instance_type = "t3.micro"
  key_name      = var.ec2_key_name

  vpc_security_group_ids = [aws_security_group.asg_backend.id]

  user_data = base64encode(templatefile("${path.module}/user_data.sh.tftpl", {
    ghcr_image           = var.ghcr_image
    db_host              = var.db_host_private_ip
    mysql_database       = var.mysql_database
    mysql_root_password  = var.mysql_root_password
    github_client_id     = var.github_client_id
    github_client_secret = var.github_client_secret
    github_pat           = var.github_pat
    gemini_api_key       = var.gemini_api_key
    frontend_url         = var.frontend_url
  }))

  tag_specifications {
    resource_type = "instance"
    tags = {
      Name = "devpilot-backend-asg"
    }
  }
}

data "aws_ssm_parameter" "ubuntu_ami" {
  name = "/aws/service/canonical/ubuntu/server/24.04/stable/current/amd64/hvm/ebs-gp3/ami-id"
}

# ── Target Group ──
resource "aws_lb_target_group" "backend" {
  name     = "devpilot-backend-tg"
  port     = 8080
  protocol = "HTTP"
  vpc_id   = var.vpc_id

  health_check {
    path                = "/api/health"
    interval            = 15
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 3
  }
}

# ── ALB ──
resource "aws_lb" "backend" {
  name               = "devpilot-backend-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = var.alb_subnet_ids
}

resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.backend.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.backend.arn
  }
}

# ── Auto Scaling Group ──
resource "aws_autoscaling_group" "backend" {
  name                = "devpilot-backend-asg"
  desired_capacity    = 1
  min_size            = 1
  max_size            = 5
  vpc_zone_identifier = [var.asg_subnet_id]
  target_group_arns   = [aws_lb_target_group.backend.arn]
  health_check_type   = "ELB"
  health_check_grace_period = 90

  launch_template {
    id      = aws_launch_template.backend.id
    version = "$Latest"
  }

  tag {
    key                 = "Name"
    value               = "devpilot-backend-asg"
    propagate_at_launch = true
  }
}

# ── 타겟 추적 스케일링 정책 (CPU 기준) ──
resource "aws_autoscaling_policy" "cpu_target" {
  name                   = "devpilot-cpu-target-tracking"
  autoscaling_group_name = aws_autoscaling_group.backend.name
  policy_type            = "TargetTrackingScaling"

  target_tracking_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ASGAverageCPUUtilization"
    }
    target_value = 50.0
  }
}