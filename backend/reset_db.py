import subprocess
import requests

BASE_URL = "http://localhost:8080/api"

print("--- Resetting Database ---")

sql_commands = """
USE medibook;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE medical_records;
TRUNCATE TABLE notifications;
TRUNCATE TABLE appointments;
TRUNCATE TABLE slots;
TRUNCATE TABLE providers;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;
"""

print("Clearing database tables...")
try:
    subprocess.run(['mysql', '-u', 'root', '-pPassword@123', '-e', sql_commands], check=True)
    print("Database cleared successfully.")
except Exception as e:
    print(f"Error clearing DB: {e}")

print("Creating Main Admin Account...")
payload = {
    "name": "Super Admin",
    "email": "admin@medibook.com",
    "password": "admin123",
    "role": "ADMIN"
}
try:
    r = requests.post(f"{BASE_URL}/auth/register", json=payload)
    if r.status_code == 200 or r.status_code == 201:
        print("Admin account created successfully!")
    else:
        print(f"Failed to create admin: {r.status_code} {r.text}")
except Exception as e:
    print(f"Error calling API: {e}")

print("--- Database Refresh Complete ---")
