output "alb_dns_name" {
  value = aws_lb.backend.dns_name
}

output "asg_backend_sg_id" {
  value = aws_security_group.asg_backend.id
}