import requests
import uuid

BASE_URL = "http://localhost:8080/api"

print("--- Slot Bug Fixes Test ---")
uid = str(uuid.uuid4())[:8]

# Register and login Provider
provider_email = f"prov_{uid}@test.com"
requests.post(f"{BASE_URL}/auth/register", json={
    "name": "Prov", "email": provider_email, "password": "password", "role": "PROVIDER",
    "specialization": "Gen", "experienceYears": 5, "clinicName": "Test", "clinicAddress": "123 Test"
})
r_prov = requests.post(f"{BASE_URL}/auth/login", json={"email": provider_email, "password": "password"})
provider_token = r_prov.json().get("token")
provider_user_id = r_prov.json()["userId"]
provider_headers = {"Authorization": f"Bearer {provider_token}"}

import subprocess
print("Approving provider via DB...")
output = subprocess.check_output(['mysql', '-u', 'root', '-pPassword@123', '-sN', '-e', f"USE medibook; UPDATE providers SET status='APPROVED', is_verified=1 WHERE user_id={provider_user_id}; SELECT id FROM providers WHERE user_id={provider_user_id};"])
provider_id = int(output.decode('utf-8').strip())

# Test Overlapping Slots
print("Testing Overlapping Slots...")
slot1_data = {"date": "2026-07-01", "startTime": "10:00", "endTime": "11:00"}
r1 = requests.post(f"{BASE_URL}/slots", json=slot1_data, headers=provider_headers)
print(f"First slot creation: {r1.status_code}")
slot1_id = r1.json()["id"]

slot2_data = {"date": "2026-07-01", "startTime": "10:30", "endTime": "11:30"}
r2 = requests.post(f"{BASE_URL}/slots", json=slot2_data, headers=provider_headers)
print(f"Second (overlapping) slot creation: {r2.status_code} {r2.text}")

# Test Re-Booking and Slot Deletion
print("Testing Re-Booking and Deletion...")

patient_email = f"pat_{uid}@test.com"
requests.post(f"{BASE_URL}/auth/register", json={"name": "Pat", "email": patient_email, "password": "password", "role": "PATIENT"})
r_pat = requests.post(f"{BASE_URL}/auth/login", json={"email": patient_email, "password": "password"})
patient_token = r_pat.json().get("token")
patient_headers = {"Authorization": f"Bearer {patient_token}"}

# Book slot 1
r = requests.post(f"{BASE_URL}/appointments", json={"providerId": provider_id, "slotId": slot1_id}, headers=patient_headers)
apt1_id = r.json()["id"]
print(f"Book 1: {r.status_code}")

# Delete slot 1 (Should fail because it's booked/pending)
r = requests.delete(f"{BASE_URL}/slots/{slot1_id}", headers=provider_headers)
print(f"Delete Slot 1 (Pending apt exists): {r.status_code} {r.text}")

# Reject appointment 1
r = requests.put(f"{BASE_URL}/appointments/{apt1_id}/reject", headers=provider_headers)
print(f"Reject apt: {r.status_code}")

# Re-book slot 1 (Should succeed now since unique constraint is gone!)
r = requests.post(f"{BASE_URL}/appointments", json={"providerId": provider_id, "slotId": slot1_id}, headers=patient_headers)
apt2_id = r.json()["id"]
print(f"Book 2 (Re-booking): {r.status_code}")

# Cancel apt 2
r = requests.put(f"{BASE_URL}/appointments/{apt2_id}/cancel", headers=patient_headers)
print(f"Cancel apt: {r.status_code}")

# Delete slot 1 (Should succeed now, it only has cancelled/rejected apts!)
r = requests.delete(f"{BASE_URL}/slots/{slot1_id}", headers=provider_headers)
print(f"Delete Slot 1 (Only inactive apts exist): {r.status_code}")

print("Tests finished.")
