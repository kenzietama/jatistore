# JatiStore Deployment Guide

Deployment architecture: Railway (Backend) + Vercel (Frontend) + Supabase (PostgreSQL)

---

## Prerequisites

- Railway account (railway.app)
- Vercel account (vercel.com)
- Supabase account (supabase.com)
- GitHub repository connected

---

## 1. Supabase Database Setup

### Create Project
1. Go to https://supabase.com/dashboard
2. Click "New Project"
3. Choose region (closest to users)
4. Set strong database password
5. Wait for provisioning (~2 minutes)

### Get Connection Details
Navigate to Project Settings → Database:
- **Connection String** (Transaction mode):
  ```
  postgresql://postgres:[PASSWORD]@[HOST]:5432/postgres?sslmode=require
  ```
- Copy for Railway env vars

### Run Migrations
Supabase starts empty - Flyway migrations run automatically on first Railway deployment.

---

## 2. Railway Backend Deployment

### Create Project
1. Go to https://railway.app/dashboard
2. Click "New Project" → "Deploy from GitHub repo"
3. Select JatiStore repository
4. Choose `backend/` as root directory

### Configure Service
Railway auto-detects Dockerfile. If not:
- **Root Directory**: `backend`
- **Builder**: Dockerfile

### Environment Variables
Add in Railway dashboard (Variables tab):

```bash
# Spring Profile
SPRING_PROFILES_ACTIVE=prod

# Database (from Supabase)
DATABASE_URL=postgresql://postgres:PASSWORD@HOST:6543/postgres?sslmode=require
DB_USERNAME=postgres
DB_PASSWORD=your_supabase_password

# JWT Secret (generate 64+ char random string)
JWT_SECRET=REPLACE_WITH_SECURE_RANDOM_STRING_MIN_64_CHARS_FOR_HS512

# Cloudinary (if used)
CLOUDINARY_URL=cloudinary://API_KEY:API_SECRET@CLOUD_NAME

# CORS (add after Vercel deployment)
ALLOWED_ORIGINS=https://your-app.vercel.app

# Port (Railway auto-assigns, but set fallback)
PORT=8080
```

### Generate JWT Secret
Use strong random generator:
```bash
openssl rand -base64 64 | tr -d '\n'
```

### Deploy
1. Railway auto-deploys on push to main/development
2. Check build logs for errors
3. Copy Railway public URL: `https://your-app.railway.app`

---

## 3. Vercel Frontend Deployment

### Create Project
1. Go to https://vercel.com/dashboard
2. Click "Add New" → "Project"
3. Import JatiStore GitHub repo
4. Framework: **Vite**
5. Root Directory: `frontend`

### Build Settings
- **Build Command**: `npm run build` (auto-detected)
- **Output Directory**: `dist` (auto-detected)
- **Install Command**: `npm install` (auto-detected)

### Environment Variables
Add in Vercel dashboard (Settings → Environment Variables):

```bash
VITE_BASE_URL=https://your-backend.railway.app
```

Replace with actual Railway backend URL.

### Deploy
1. Click "Deploy"
2. Wait for build (~2-3 minutes)
3. Copy Vercel URL: `https://your-app.vercel.app`

---

## 4. Final Configuration

### Update Railway CORS
1. Go back to Railway project
2. Add Vercel URL to `ALLOWED_ORIGINS`:
   ```
   ALLOWED_ORIGINS=https://your-app.vercel.app
   ```
3. Railway auto-redeploys

### Test Deployment
1. Visit Vercel frontend URL
2. Try registration/login flow
3. Check browser console for CORS errors
4. Check Railway logs for backend errors

---

## 5. Custom Domains (Optional)

### Vercel Custom Domain
1. Go to Project Settings → Domains
2. Add custom domain
3. Update DNS records (A/CNAME)
4. SSL auto-provisions

### Update CORS
Add custom domain to Railway `ALLOWED_ORIGINS`:
```
ALLOWED_ORIGINS=https://your-app.vercel.app,https://custom-domain.com
```

---

## 6. Production Checklist

- [ ] Database migrations ran successfully (check Railway logs)
- [ ] JWT secret is secure random string (min 64 chars)
- [ ] CORS configured with Vercel domain
- [ ] Cloudinary credentials set (if using image uploads)
- [ ] Frontend connects to Railway backend (check Network tab)
- [ ] Login/register flow works
- [ ] Flash sales load correctly
- [ ] Checkout flow completes
- [ ] No console errors in browser
- [ ] No 500 errors in Railway logs

---

## 7. Troubleshooting

### Frontend shows "Network Error"
- Check `VITE_BASE_URL` in Vercel env vars
- Verify Railway backend is running (check logs)

### Backend 500 errors
- Check Railway logs for stack traces
- Verify `DATABASE_URL` format (must include `?sslmode=require`)
- Check Flyway migrations ran successfully

### CORS errors in browser
- Verify `ALLOWED_ORIGINS` includes Vercel URL
- Check Railway redeployed after env var change
- Ensure no trailing slashes in URLs

### Database connection failed
- Verify Supabase password in Railway env vars
- Check Supabase project is not paused
- Ensure connection pooler port (6543) used if high traffic

---

## 8. Monitoring

### Railway Logs
```bash
# Install Railway CLI
npm install -g @railway/cli

# Login
railway login

# View logs
railway logs
```

### Vercel Logs
Check Deployments tab → Select deployment → View Function Logs

---

## Cost Estimates

- **Supabase**: Free tier (500MB DB, 50MB file storage)
- **Railway**: $5/month usage-based (500 hours free trial)
- **Vercel**: Free tier (100GB bandwidth, unlimited deployments)

**Total**: ~$5/month for production deployment
