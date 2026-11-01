# Lovely Tasks 🌸

A Java web application for managing daily tasks with a modern pink & white aesthetic, backed by MySQL on AWS EC2 and packaged as a standard Web Application Archive (`.war`) for Apache Tomcat 10+.

---

## Tech Stack

- **Backend**: Java Servlets (Jakarta EE 5.0 / 6.0), JDBC
- **JSON Processing**: Google Gson
- **Frontend**: Vanilla JavaScript, HTML5, CSS3
- **Database**: MySQL 8.x (running on EC2)
- **Application Server**: Apache Tomcat 10.x / 11.x
- **Build**: Maven / Standalone build scripts (`build.bat` & `build.ps1`)

---

## Project Structure

```text
todo-app/
├── database/
│   ├── schema.sql              # Database creation and table definition
│   └── setup_ec2_mysql.sh      # Automated setup script for Ubuntu EC2
├── lib/                        # Local JAR dependencies for non-Maven builds
├── src/
│   ├── main/
│   │   ├── java/com/lovely/todo/
│   │   │   ├── controller/
│   │   │   │   └── TodoServlet.java
│   │   │   ├── dao/
│   │   │   │   ├── DBUtil.java
│   │   │   │   └── TodoDAO.java
│   │   │   └── model/
│   │   │       └── Todo.java
│   │   ├── resources/
│   │   │   └── db.properties   # Database connection configuration
│   │   └── webapp/
│   │       ├── css/style.css
│   │       ├── js/app.js
│   │       ├── index.html
│   │       └── WEB-INF/web.xml
├── build.bat                   # Windows CMD build script
├── build.ps1                   # Windows PowerShell build script
├── pom.xml                     # Maven build configuration
└── README.md
```

---

## Step 1: Set Up MySQL on EC2 (`100.53.19.151`)

### 1. Configure AWS Security Group
Before connecting from outside the EC2 instance, ensure port `3306` is open:
1. Open the **AWS Management Console** -> **EC2** -> **Instances**.
2. Select your MySQL instance and click the **Security** tab.
3. Click your **Security Group** -> **Edit inbound rules** -> **Add rule**:
   - **Type**: `MySQL/Aurora` (Port `3306`)
   - **Source**: `0.0.0.0/0` (or the IP of your Tomcat host)
4. Click **Save rules**.

### 2. Configure MySQL on the EC2 Server
SSH into your EC2 machine and run either the automated script or the manual steps below:

#### Option A: One-line Automated Setup (Ubuntu/Debian)
```bash
curl -sSL https://raw.githubusercontent.com/<YOUR_USER>/<YOUR_REPO>/main/database/setup_ec2_mysql.sh | bash
```

#### Option B: Manual Setup
1. Edit the MySQL configuration file to allow remote connections:
   ```bash
   sudo sed -i 's/^bind-address\s*=.*/bind-address = 0.0.0.0/' /etc/mysql/mysql.conf.d/mysqld.cnf
   sudo systemctl restart mysql
   ```

2. Open the MySQL console:
   ```bash
   sudo mysql -u root
   ```

3. Paste the following SQL commands:
   ```sql
   CREATE DATABASE IF NOT EXISTS tododb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

   CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY 'password123';
   ALTER USER 'root'@'%' IDENTIFIED BY 'password123';
   GRANT ALL PRIVILEGES ON tododb.* TO 'root'@'%' WITH GRANT OPTION;
   FLUSH PRIVILEGES;

   USE tododb;

   CREATE TABLE IF NOT EXISTS todos (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       title VARCHAR(255) NOT NULL,
       category VARCHAR(100) DEFAULT 'Personal 🌷',
       priority VARCHAR(20) DEFAULT 'MEDIUM',
       completed BOOLEAN DEFAULT FALSE,
       due_date VARCHAR(50),
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
   ```

---

## Step 2: Clone the Repository

Clone the project to your local development machine or Tomcat deployment server:

```bash
git clone https://github.com/<YOUR_USER>/<YOUR_REPO>.git
cd <YOUR_REPO>/todo-app
```

---

## Step 3: Configure Database Connection

Open `src/main/resources/db.properties` and verify your EC2 connection details:

```properties
db.host=100.53.19.151
db.port=3306
db.name=tododb
db.user=root
db.password=password123
db.connectTimeout=3000
db.socketTimeout=5000
```

> **Note**: You can also override any of these settings at runtime without modifying files by setting environment variables on your server: `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`.

---

## Step 4: Build the WAR Package

You can build the project using standard Maven or the bundled standalone build scripts:

### Option A: Using Maven (Recommended)
```bash
mvn clean package
```
The output file will be generated at: `target/todo-app.war`.

### Option B: Using PowerShell (Windows)
```powershell
.\build.ps1
```

### Option C: Using Command Prompt (Windows)
```cmd
build.bat
```

---

## Step 5: Deploy to Apache Tomcat

1. Copy the generated `target/todo-app.war` into your Tomcat `webapps/` directory:
   - **Linux**:
     ```bash
     cp target/todo-app.war /opt/tomcat/webapps/
     ```
   - **Windows**:
     Copy `target\todo-app.war` into `C:\Program Files\Apache Software Foundation\Tomcat 10.x\webapps\`

2. If Tomcat is running, it will automatically unpack and deploy the archive.

3. Open your browser and navigate to:
   ```text
   http://localhost:8080/todo-app/
   ```
   *(Replace `localhost` with your server's IP address if deploying on a remote machine)*

---

## Step 6: Verify Database Records

To confirm that tasks created from the web interface are persisting into MySQL:

1. Log into your EC2 server.
2. Run:
   ```bash
   sudo mysql -u root -e "SELECT id, title, category, priority, completed, created_at FROM tododb.todos;"
   ```
3. Newly added tasks will appear in the query result.

---

## Troubleshooting

### Connection Timed Out
- Verify that AWS EC2 Security Group allows Inbound TCP traffic on port `3306`.
- Verify `bind-address = 0.0.0.0` in `/etc/mysql/mysql.conf.d/mysqld.cnf`.
- Check MySQL status on EC2: `sudo systemctl status mysql`.

### Access Denied for User
- Ensure the user has the `%` host wildcard granted (`'root'@'%'` or a custom user).
- Verify password in `src/main/resources/db.properties` matches the user password in MySQL.

### Tomcat ClassNotFoundException / LifecycleException
- This project targets **Jakarta EE (Tomcat 10.x / 11.x)**. If deploying on legacy Tomcat 9 or older (`javax.servlet`), upgrade your Tomcat installation to Tomcat 10.0+ or 10.1+.
