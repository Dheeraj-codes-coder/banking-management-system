# Free Hosting Guide: Koyeb + TiDB Cloud

This setup hosts the Spring Boot frontend and backend together on Koyeb and
stores the data in TiDB Cloud. TiDB is compatible with MySQL, so the existing
MySQL Connector/J and Spring Data JPA code can remain unchanged.

Do not place a real database password or admin password inside GitHub files.
Add them only as Koyeb environment variables.

## 1. Test the Docker image (optional)

Open PowerShell inside the `banking-app` folder and run:

```powershell
docker build -t banking-app .
```

If the command ends successfully, the deployment image is ready. Your normal
local Spring Boot run continues to use MySQL on `localhost:3306`.

## 2. Upload the project to GitHub

1. Sign in to GitHub.
2. Click **New repository**.
3. Repository name: `banking-management-system`.
4. Select **Public** (best for a portfolio) or **Private**.
5. Do not add a README, `.gitignore`, or license because the project already
   contains its own files.
6. Click **Create repository**.

### Upload with Git

Open PowerShell inside the `banking-app` folder. Replace `YOUR_USERNAME` in the
last two commands:

```powershell
git init
git branch -M main
git add .
git commit -m "Prepare banking app for cloud deployment"
git remote add origin https://github.com/YOUR_USERNAME/banking-management-system.git
git push -u origin main
```

If Windows says that `git` is not recognized, install Git for Windows and open
PowerShell again. You can also use GitHub's **uploading an existing file** page
and upload everything inside the `banking-app` folder.

Before continuing, open the repository on GitHub and confirm that these files
are visible at its top level:

- `Dockerfile`
- `pom.xml`
- `DEPLOYMENT_FREE.md`
- `src`

## 3. Create the free database

1. Open <https://tidbcloud.com> and sign in.
2. Create a **TiDB Cloud Starter** instance using the **Free** plan.
3. Choose an AWS Frankfurt region if it is available. This keeps it close to
   Koyeb's free Frankfurt web-service region.
4. Name the instance `banking-db` and wait until it is ready.
5. Open its SQL Editor and run:

```sql
CREATE DATABASE IF NOT EXISTS banking_app_project;
```

6. Click **Connect**.
7. Select **Public**, branch **main**, and **General**.
8. Click **Generate Password** and save the password somewhere safe. TiDB may
   show it only once.
9. Copy the displayed host and username. The port is normally `4000`.

You will use these values in the next section:

```text
Host: gateway...tidbcloud.com
Port: 4000
Username: ...root
Password: your generated password
```

## 4. Deploy the Docker project on Koyeb

1. Open <https://app.koyeb.com> and sign in with GitHub.
2. Click **Create Web Service**.
3. Select **GitHub** as the source and authorize your GitHub account.
4. Select the `banking-management-system` repository and the `main` branch.
5. Select **Dockerfile** as the builder.
6. Set the application name to `banking-app`.
7. Choose the **Free** instance and **Frankfurt** region.
8. Under **Ports**, expose port `8080` using HTTP with the `/` route.
9. Add every environment variable from the following table. Replace all
   placeholder values with your own values.

| Variable | Value |
| --- | --- |
| `PORT` | `8080` |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://YOUR_TIDB_HOST:4000/banking_app_project?sslMode=VERIFY_IDENTITY` |
| `SPRING_DATASOURCE_USERNAME` | Your TiDB username |
| `SPRING_DATASOURCE_PASSWORD` | Your TiDB generated password |
| `BANK_ADMIN_EMAIL` | Your admin email |
| `BANK_ADMIN_PASSWORD` | A new strong password, for example 12+ characters |
| `SPRING_JPA_SHOW_SQL` | `false` |
| `DB_MAX_POOL_SIZE` | `5` |
| `DB_MIN_IDLE` | `1` |

10. Click **Deploy**. The first Docker build can take several minutes.
11. Watch the runtime logs. Deployment is ready when the log contains
    `Started BankingAppApplication`.
12. Click the generated address ending in `.koyeb.app`.

## 5. Test the hosted application

1. Open the generated Koyeb URL. The banking login page should appear.
2. Register a new customer.
3. Log in using that customer's email and password.
4. Log out, then sign in with the admin email and password entered in Koyeb.
5. Open Customers, Accounts, and Transactions from the admin dashboard.

The cloud database starts empty. Local MySQL customers and accounts are not
automatically copied to TiDB Cloud.

## 6. If deployment fails

### `Access denied for user`

Re-copy the TiDB username and password into Koyeb. Do not add quotation marks.

### `Communications link failure` or SSL error

Confirm that the URL starts with `jdbc:mysql://`, uses port `4000`, contains
the correct public host, and ends with `?sslMode=VERIFY_IDENTITY`.

### Koyeb shows an unhealthy service

Confirm the exposed HTTP port is `8080` and the `PORT` variable is `8080`.

### Container exits because of memory

Confirm that you deployed the included Dockerfile and did not remove its
`JAVA_TOOL_OPTIONS`. The free instance has limited memory.

## Free-tier limitations

- Koyeb provides one free web instance with limited CPU and 512 MB RAM.
- The service scales to zero after a period without traffic, so the first visit
  after inactivity can be slower.
- TiDB Cloud Starter is free only while the database remains within its free
  storage and request quotas.
- This setup is suitable for learning, demonstrations, and a portfolio. Use a
  paid service before accepting real banking or sensitive customer data.
