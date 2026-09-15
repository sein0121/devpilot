variable "vpc_id" {
  default = "vpc-0f63b37703a8b37ee"
}

variable "alb_subnet_ids" {
  description = "ALB용 서브넷 (최소 2개 AZ 필요)"
  default     = ["subnet-05f7d8b4bcb4da6c0", "subnet-0be0e2917e05f3f7f"]
}

variable "asg_subnet_id" {
  description = "ASG 인스턴스가 뜰 서브넷 (기존 EC2와 동일 AZ)"
  default     = "subnet-05f7d8b4bcb4da6c0"
}

variable "db_host_private_ip" {
  default = "172.31.40.181"
}

variable "db_host_security_group_id" {
  default = "sg-026dbe3fe1d5b80dd"
}

variable "ghcr_image" {
  default = "ghcr.io/sein0121/devpilot-backend:latest"
}

variable "ec2_key_name" {
  default = "devpilot-ec2-key-seoul"
}

variable "mysql_database" {
  type = string
}
variable "mysql_root_password" {
  type      = string
  sensitive = true
}
variable "github_client_id" {
  type = string
}
variable "github_client_secret" {
  type      = string
  sensitive = true
}
variable "github_pat" {
  type      = string
  sensitive = true
}
variable "gemini_api_key" {
  type      = string
  sensitive = true
}
variable "frontend_url" {
  type = string
}