Production deployment notes and examples

1) Purpose
This file explains how to run the application in production profile on an EC2 instance and how to securely supply environment variables using AWS services.

2) application-prod.properties
- See src/main/resources/application-prod.properties (placeholders for DB_URL, DB_USER, DB_PASSWORD, ALLOWED_ORIGINS, PORT)

3) Build the artifact locally

Windows (PowerShell):
```
cd blog_post_backend
.
\mvnw.cmd clean package -DskipTests
```

Linux / Mac:
```
cd blog_post_backend
./mvnw clean package -DskipTests
```

The jar will be at `target/*.jar`.

4) Copy to EC2 and run as systemd service

- Create an IAM role for EC2 with permission to read SSM Parameter Store or Secrets Manager (if used).
- Launch EC2 (Amazon Linux 2023 or Ubuntu 22.04) with that IAM role and a security group allowing 22, 80, 443, and 8080 as needed.
- Copy jar to instance:
```
scp target/blog_post_backend-0.0.1-SNAPSHOT.jar ec2-user@<ec2-ip>:/home/ec2-user/backend.jar
```
- Create systemd unit `/etc/systemd/system/blog-backend.service`:
```
[Unit]
Description=Blog Post Backend
After=network.target

[Service]
User=ec2-user
WorkingDirectory=/opt/blog
ExecStart=/usr/bin/java -jar /opt/blog/backend.jar --spring.profiles.active=prod
EnvironmentFile=/etc/blog/backend.env
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

- Use an `EnvironmentFile` to populate env vars (see next section).

5) Hiding environment variables: SSM Parameter Store vs Secrets Manager

- Option A: SSM Parameter Store (free tier friendly)
  - Put secrets as SecureString in Parameter Store.
  - Attach IAM policy to EC2 role to allow `ssm:GetParameter` for those names.
  - On instance boot, use the AWS CLI to fetch and write `/etc/blog/backend.env` before starting service.
  - Example command on EC2 (with IAM role):
    ```bash
    aws ssm get-parameter --name "/blog/backend/DB_PASSWORD" --with-decryption --query Parameter.Value --output text > /etc/blog/.dbpass
    ```
  - Then write other env vars to `/etc/blog/backend.env` as `DB_PASSWORD=...`.

- Option B: AWS Secrets Manager (managed, rotation supported)
  - Store a JSON secret with username/password/host.
  - Attach `secretsmanager:GetSecretValue` permission to EC2 role.
  - Use AWS CLI or SDK to fetch at boot and write `/etc/blog/backend.env`.

Automated fetch example (EC2 user-data or boot script):
```
mkdir -p /etc/blog
DB_URL=$(aws ssm get-parameter --name /blog/backend/DB_URL --with-decryption --query Parameter.Value --output text)
DB_USER=$(aws ssm get-parameter --name /blog/backend/DB_USER --with-decryption --query Parameter.Value --output text)
DB_PASSWORD=$(aws ssm get-parameter --name /blog/backend/DB_PASSWORD --with-decryption --query Parameter.Value --output text)
cat > /etc/blog/backend.env <<EOF
DB_URL="$DB_URL"
DB_USER="$DB_USER"
DB_PASSWORD="$DB_PASSWORD"
ALLOWED_ORIGINS="https://your-cloudfront-domain.cloudfront.net"
EOF
```

6) Using H2 on AWS

- H2 file mode stores a file on the instance filesystem. This will work only while the EC2 instance persists its EBS volume. It is NOT suitable for multi-instance or auto-scaling setups. If the instance is terminated, you lose data unless you persist the `data/` directory to an EBS volume or EFS.
- For production, recommended options:
  - Use AWS RDS (Postgres or MySQL). RDS has a free tier (single-AZ db.t4g.micro) for 12 months for new accounts.
  - Use Amazon Aurora Serverless (not free). For learning/demo, RDS Postgres is simplest.

7) Quick RDS + application-prod.properties mapping

- Create an RDS Postgres instance and note the JDBC URL: `jdbc:postgresql://<host>:5432/<db>`
- Set SSM params or Secrets Manager keys described earlier for `DB_URL`, `DB_USER`, `DB_PASSWORD`.

8) CloudFront as both frontend and backend domain

- You can use a CloudFront distribution pointing to S3 for frontend and another origin (the EC2 public DNS or ALB) for API under a different path, OR use a separate CloudFront distribution for API traffic. Simpler: use one CloudFront for frontend and point the frontend's `environment.apiUrl` to the EC2 public DNS (HTTPS via ALB) or to an API-specific CloudFront distribution.
- If you use CloudFront for API, ensure that the distribution forwards the appropriate headers and supports POST/PATCH/PUT methods and CORS preflight.

9) Start and enable service

```
sudo mkdir -p /opt/blog
sudo mv /home/ec2-user/backend.jar /opt/blog/backend.jar
sudo chown -R ec2-user:ec2-user /opt/blog
sudo systemctl daemon-reload
sudo systemctl enable blog-backend
sudo systemctl start blog-backend
sudo journalctl -u blog-backend -f
```

10) Notes
- Keep H2 for local dev only. Use RDS Postgres for production durability.
- Never check secrets into Git. Use Parameter Store or Secrets Manager + IAM role.
- For CI/CD, store deploy keys and AWS credentials in GitHub Secrets.
