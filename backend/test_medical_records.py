import requests
import time

BASE_URL = "http://localhost:8080/api"

print("--- End to End Medical Record Test ---")

# 1. Register Patient
patient_data = {"name": "Record Patient", "email": "record_patient@test.com", "password": "password", "role": "PATIENT"}
requests.post(f"{BASE_URL}/auth/register", json=patient_data)
r = requests.post(f"{BASE_URL}/auth/login", json={"email": "record_patient@test.com", "password": "password"})
patient_token = r.json().get("token")
print(f"Patient registered. Token: {'Yes' if patient_token else 'No'}")

# 2. Register Provider
provider_data = {
    "name": "Record Provider", "email": "record_provider@test.com", "password": "password", "role": "PROVIDER",
    "specialization": "General", "experienceYears": 5, "clinicName": "Test Clinic", "clinicAddress": "123 Test St"
}
requests.post(f"{BASE_URL}/auth/register", json=provider_data)
r = requests.post(f"{BASE_URL}/auth/login", json={"email": "record_provider@test.com", "password": "password"})
provider_token = r.json().get("token")
print(f"Provider registered. Token: {'Yes' if provider_token else 'No'}")

provider_headers = {"Authorization": f"Bearer {provider_token}"}
patient_headers = {"Authorization": f"Bearer {patient_token}"}

# Approve Provider via DB query using Python (simulating admin)
import subprocess
print("Approving provider via DB...")
subprocess.run(['mysql', '-u', 'root', '-pPassword@123', '-e', "USE medibook; UPDATE providers SET status='APPROVED', is_verified=1 WHERE user_id=(SELECT id FROM users WHERE email='record_provider@test.com');"])

# Get Provider ID
output = subprocess.check_output(['mysql', '-u', 'root', '-pPassword@123', '-sN', '-e', "USE medibook; SELECT id FROM providers WHERE user_id=(SELECT id FROM users WHERE email='record_provider@test.com');"])
provider_id = int(output.decode('utf-8').strip())
print(f"Provider ID from DB: {provider_id}")

# 3. Create Slot
slot_data = {"date": "2026-05-01", "startTime": "10:00", "endTime": "10:30"}
r = requests.post(f"{BASE_URL}/slots", json=slot_data, headers=provider_headers)
slot_id = r.json()["id"]
print(f"Slot created: {slot_id}")

# 4. Book Appointment
r = requests.post(f"{BASE_URL}/appointments", json={"providerId": provider_id, "slotId": slot_id}, headers=patient_headers)
apt_id = r.json()["id"]
print(f"Appointment booked: {apt_id}")

# 5. Try to create Record before complete (Should Fail)
payload = {
    "appointmentId": apt_id,
    "diagnosis": "Test Diagnosis",
    "prescription": "Test Prescription",
    "notes": "Test Notes",
    "followUpDate": "2026-05-10"
}
r = requests.post(f"{BASE_URL}/records", json=payload, headers=provider_headers)
print(f"Create Record (Before Complete): {r.status_code} - {r.text}")

# 6. Complete Appointment
requests.put(f"{BASE_URL}/appointments/{apt_id}/accept", headers=provider_headers)
requests.put(f"{BASE_URL}/appointments/{apt_id}/complete", headers=provider_headers)
print("Appointment completed.")

# 7. Create Record (Should Succeed)
r = requests.post(f"{BASE_URL}/records", json=payload, headers=provider_headers)
print(f"Create Record (After Complete): {r.status_code}")
if r.status_code != 201:
    print(r.text)
    exit(1)
record_id = r.json()["recordId"]
patient_id = r.json()["patientId"]

# 8. Patient access
r = requests.get(f"{BASE_URL}/records/appointment/{apt_id}", headers=patient_headers)
print(f"Patient get by appointment: {r.status_code}")

r = requests.get(f"{BASE_URL}/records/patient/{patient_id}", headers=patient_headers)
print(f"Patient get by patient ID: {r.status_code} - Count: {len(r.json())}")

# 9. Provider update
payload["notes"] = "Updated Notes!"
r = requests.put(f"{BASE_URL}/records/{record_id}", json=payload, headers=provider_headers)
print(f"Provider update record: {r.status_code} - Notes: {r.json()['notes']}")

# 10. Provider get follow-ups
r = requests.get(f"{BASE_URL}/records/followups", headers=provider_headers)
print(f"Provider get followups: {r.status_code} - Count: {len(r.json())}")

print("Test Completed!")
