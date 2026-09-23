# Dhaka Tesla Pool — Deployment Guide (Render & Aiven)

Step-by-step instructions to deploy the Dhaka Tesla Pool full-stack project to **Aiven** (Free Managed MySQL) and **Render** (Cloud Hosting for Spring Boot & React).

---

## Part 1: Set Up MySQL on Aiven

1. Sign up or log in at **[aiven.io](https://aiven.io/)**.
2. Click **Create Service**.
3. Choose **MySQL**.
4. Select the **Free Tier** plan and your closest cloud region.
5. Name your service (e.g., `dhaka-tesla-db`) and click **Create Service**.
6. Once the service is running, find the **Connection Details** on the Overview tab:
   - **Host**: e.g., `mysql-xxxxx.aivencloud.com`
   - **Port**: e.g., `12345`
   - **User**: `avnadmin`
   - **Password**: (click to copy)
   - **Database Name**: `defaultdb`
7. Form your Spring Boot JDBC URL:
   ```text
   jdbc:mysql://<HOST>:<PORT>/defaultdb?useSSL=false&allowPublicKeyRetrieval=true
   ```

---

## Part 2: Deploy Backend to Render (Spring Boot)

1. Sign up or log in at **[render.com](https://render.com/)**.
2. Click **New +** → **Web Service**.
3. Connect your GitHub repository (`Dhaka-Tesla`).
4. Configure the Web Service:
   - **Name**: `dhaka-tesla-backend`
   - **Region**: Same region as your Aiven database
   - **Branch**: `master` (or your active branch)
   - **Runtime**: **Docker** (Render will automatically detect `Dockerfile` at root)
   - **Instance Type**: **Free**
5. Scroll down to **Environment Variables** and add:
   - `SPRING_DATASOURCE_URL`: `jdbc:mysql://<HOST>:<PORT>/defaultdb?useSSL=false&allowPublicKeyRetrieval=true`
   - `SPRING_DATASOURCE_USERNAME`: `avnadmin`
   - `SPRING_DATASOURCE_PASSWORD`: `<YOUR_AIVEN_PASSWORD>`
6. Click **Create Web Service**.
7. Render will build the Docker container, run migrations, seed data (Jashim, Bullet, Nusrat, Rafiq, Shirin), and start the server.
8. Once live, copy your backend URL: e.g. `https://dhaka-tesla-backend.onrender.com`.

---

## Part 3: Deploy Frontend to Render (React Static Site)

1. In Render, click **New +** → **Static Site**.
2. Connect the same repository.
3. Configure the Static Site:
   - **Name**: `dhaka-tesla-frontend`
   - **Branch**: `master`
   - **Root Directory**: `frontend`
   - **Build Command**: `npm install && npm run build`
   - **Publish Directory**: `dist`
4. Under **Environment Variables**, add:
   - `VITE_API_BASE_URL`: `https://dhaka-tesla-backend.onrender.com/api`
   *(Replace with your actual Render backend URL)*
5. Click **Create Static Site**.
6. Render will build and deploy the React app. The `_redirects` file in `public/` ensures SPA routing works seamlessly.

---

## Alternative: Deploy via Render Blueprint (`render.yaml`)

This repository includes a preconfigured [render.yaml](file:///d:/pramit%20java/Dhaka-Tesla/render.yaml):

1. Go to **Render Dashboard** → **Blueprints** → **New Blueprint Instance**.
2. Select your repository.
3. Render automatically detects both `dhaka-tesla-backend` and `dhaka-tesla-frontend`.
4. Enter your Aiven MySQL credentials when prompted.
5. Click **Apply** to deploy both services simultaneously.
