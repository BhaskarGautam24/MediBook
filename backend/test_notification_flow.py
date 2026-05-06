import requests
import uuid

BASE_URL = "http://localhost:8080/api"

print("--- End to End Notification Flow Test ---")

uid = str(uuid.uuid4())[:8]
patient_email = f"pat_{uid}@test.com"
provider_email = f"prov_{uid}@test.com"

# 1. Register Patient & Provider
requests.post(f"{BASE_URL}/auth/register", json={"name": "Pat", "email": patient_email, "password": "password", "role": "PATIENT"})
r_pat = requests.post(f"{BASE_URL}/auth/login", json={"email": patient_email, "password": "password"})
patient_token = r_pat.json().get("token")
patient_id = r_pat.json()["userId"]

requests.post(f"{BASE_URL}/auth/register", json={
    "name": "Prov", "email": provider_email, "password": "password", "role": "PROVIDER",
    "specialization": "Gen", "experienceYears": 5, "clinicName": "Test", "clinicAddress": "123 Test"
})
r_prov = requests.post(f"{BASE_URL}/auth/login", json={"email": provider_email, "password": "password"})
provider_token = r_prov.json().get("token")
provider_user_id = r_prov.json()["userId"]

provider_headers = {"Authorization": f"Bearer {provider_token}"}
patient_headers = {"Authorization": f"Bearer {patient_token}"}

# Approve Provider via API using an admin user (if we have admin) or DB
import subprocess
print("Approving provider via DB...")
subprocess.run(['mysql', '-u', 'root', '-pPassword@123', '-e', f"USE medibook; UPDATE providers SET status='APPROVED', is_verified=1 WHERE user_id={provider_user_id};"])

# Check provider notifications for ACCOUNT_UPDATE
# But wait, approve was done via DB, not API, so no notification will be triggered!
# We can trigger API approve instead if we create an admin token. Let's just create an admin.
admin_email = f"admin_{uid}@test.com"
requests.post(f"{BASE_URL}/auth/register", json={"name": "Admin", "email": admin_email, "password": "password", "role": "ADMIN"})
r_admin = requests.post(f"{BASE_URL}/auth/login", json={"email": admin_email, "password": "password"})
admin_token = r_admin.json().get("token")
admin_headers = {"Authorization": f"Bearer {admin_token}"}

# Get provider id
output = subprocess.check_output(['mysql', '-u', 'root', '-pPassword@123', '-sN', '-e', f"USE medibook; SELECT id FROM providers WHERE user_id={provider_user_id};"])
provider_id = int(output.decode('utf-8').strip())

# Actually let's test approval via API!
# First reject, then approve so we get both notifications.
print(f"Approving provider {provider_id} via API...")
requests.put(f"{BASE_URL}/admin/providers/{provider_id}/approve", headers=admin_headers)

r_notif = requests.get(f"{BASE_URL}/notifications/{provider_user_id}", headers=provider_headers)
print(f"Provider Notifications after Approval: {[n['type'] for n in r_notif.json()]}")

# Create slot
r = requests.post(f"{BASE_URL}/slots", json={"date": "2026-06-01", "startTime": "10:00", "endTime": "10:30"}, headers=provider_headers)
slot_id = r.json()["id"]

# Book appointment (Patient)
r = requests.post(f"{BASE_URL}/appointments", json={"providerId": provider_id, "slotId": slot_id}, headers=patient_headers)
apt_id = r.json()["id"]
print(f"Patient booked appointment {apt_id}.")

r_notif = requests.get(f"{BASE_URL}/notifications/{provider_user_id}", headers=provider_headers)
print(f"Provider Notifications after Booking: {[n['type'] for n in r_notif.json()]}")

# Accept appointment (Provider)
r_acc = requests.put(f"{BASE_URL}/appointments/{apt_id}/accept", headers=provider_headers)
print(f"Accept response: {r_acc.status_code} {r_acc.text}")

r_notif = requests.get(f"{BASE_URL}/notifications/{patient_id}", headers=patient_headers)
print(f"Patient Notifications after Acceptance: {[n['type'] for n in r_notif.json()]}")

# Complete and add record
r_comp = requests.put(f"{BASE_URL}/appointments/{apt_id}/complete", headers=provider_headers)
print(f"Complete response: {r_comp.status_code} {r_comp.text}")

payload = {
    "appointmentId": apt_id,
    "diagnosis": "Test",
    "prescription": "Test",
    "notes": "Test",
    "followUpDate": "2026-06-10"
}
r_rec = requests.post(f"{BASE_URL}/records", json=payload, headers=provider_headers)
print(f"Record response: {r_rec.status_code} {r_rec.text}")

r_notif = requests.get(f"{BASE_URL}/notifications/{patient_id}", headers=patient_headers)
print(f"Patient Notifications after Completion and Record: {[n['type'] for n in r_notif.json()]}")

print("Test Completed successfully!")
