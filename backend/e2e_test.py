import requests

BASE_URL = "http://localhost:8080/api"

print("--- Final E2E Test Workflow ---")

# 1. Login Patient
r_pat = requests.post(f"{BASE_URL}/auth/login", json={"email": "patient@gmail.com", "password": "123456"})
patient_token = r_pat.json().get("token")
patient_id = r_pat.json()["userId"]
patient_headers = {"Authorization": f"Bearer {patient_token}"}
print("Patient logged in.")

# 2. Login Provider
r_prov = requests.post(f"{BASE_URL}/auth/login", json={"email": "doctor@gmail.com", "password": "123456"})
provider_token = r_prov.json().get("token")
provider_user_id = r_prov.json()["userId"]
provider_headers = {"Authorization": f"Bearer {provider_token}"}
print("Provider logged in.")

# 3. Provider Adds Slot
import datetime
tomorrow = (datetime.date.today() + datetime.timedelta(days=1)).isoformat()
r_slot = requests.post(f"{BASE_URL}/slots", json={"date": tomorrow, "startTime": "09:00", "endTime": "09:30"}, headers=provider_headers)
slot_id = r_slot.json()["id"]
provider_id = r_slot.json()["providerId"]
print(f"Provider created slot {slot_id} for {tomorrow} at 09:00.")

# 4. Patient Books Appointment
r_book = requests.post(f"{BASE_URL}/appointments", json={"providerId": provider_id, "slotId": slot_id}, headers=patient_headers)
apt_id = r_book.json()["id"]
print(f"Patient booked appointment {apt_id}.")

# 5. Provider Checks Notifications
r_notif = requests.get(f"{BASE_URL}/notifications/{provider_user_id}", headers=provider_headers)
print(f"Provider Notifications: {[n['type'] for n in r_notif.json()]}")

# 6. Provider Attempts to Delete Slot (Should Fail)
r_del = requests.delete(f"{BASE_URL}/slots/{slot_id}", headers=provider_headers)
print(f"Provider attempted to delete slot (expected to fail): {r_del.status_code}")

# 7. Provider Accepts Appointment
requests.put(f"{BASE_URL}/appointments/{apt_id}/accept", headers=provider_headers)
print("Provider accepted appointment.")

# 8. Patient Checks Notifications
r_notif = requests.get(f"{BASE_URL}/notifications/{patient_id}", headers=patient_headers)
print(f"Patient Notifications: {[n['type'] for n in r_notif.json()]}")

# 9. Provider Completes Appointment & Adds Record
requests.put(f"{BASE_URL}/appointments/{apt_id}/complete", headers=provider_headers)
payload = {
    "appointmentId": apt_id,
    "diagnosis": "Routine Checkup",
    "prescription": "Vitamins",
    "notes": "Patient is healthy",
    "followUpDate": (datetime.date.today() + datetime.timedelta(days=30)).isoformat()
}
r_rec = requests.post(f"{BASE_URL}/records", json=payload, headers=provider_headers)
print(f"Provider completed appointment and created record {r_rec.json().get('recordId')}.")

# 10. Patient Final Notifications
r_notif = requests.get(f"{BASE_URL}/notifications/{patient_id}", headers=patient_headers)
print(f"Patient Final Notifications: {[n['type'] for n in r_notif.json()]}")

print("--- End of Workflow ---")
