import requests

BASE_URL = "http://localhost:8080/api"

print("--- Creating Test Users ---")

# Delete old users just in case
import subprocess
subprocess.run(['mysql', '-u', 'root', '-pPassword@123', '-e', "USE medibook; DELETE FROM users WHERE email='doctor@gmail.com' OR email='patient@gmail.com';"])

# Register Patient
print("Registering Patient...")
requests.post(f"{BASE_URL}/auth/register", json={
    "name": "Test Patient",
    "email": "patient@gmail.com",
    "phone": "+918393032400",
    "password": "123456",
    "role": "PATIENT"
})

# Register Provider
print("Registering Provider...")
requests.post(f"{BASE_URL}/auth/register", json={
    "name": "Dr. Test Provider",
    "email": "doctor@gmail.com",
    "password": "123456",
    "role": "PROVIDER",
    "specialization": "Cardiology",
    "experienceYears": 10,
    "clinicName": "Test Heart Clinic",
    "clinicAddress": "123 Main St"
})

# Approve Provider via DB
print("Approving Provider...")
subprocess.run(['mysql', '-u', 'root', '-pPassword@123', '-e', "USE medibook; UPDATE providers SET status='APPROVED', is_verified=1 WHERE user_id=(SELECT id FROM users WHERE email='doctor@gmail.com');"])

print("Users created and ready for UI testing!")
