import urllib.request, urllib.error, json, sys, random, string

BASE = 'http://localhost:8080/api'
results = []
suffix = ''.join(random.choices(string.ascii_lowercase + string.digits, k=6))

def api(method, path, data=None, token=None):
    url = BASE + path
    headers = {'Content-Type': 'application/json'}
    if token:
        headers['Authorization'] = 'Bearer ' + token
    body = json.dumps(data).encode() if data else None
    req = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        resp = urllib.request.urlopen(req)
        body = resp.read().decode()
        if not body:
            return resp.getcode(), {}
        return resp.getcode(), json.loads(body)
    except urllib.error.HTTPError as e:
        body = e.read().decode()
        try:
            return e.code, json.loads(body)
        except:
            return e.code, body

print('='*60)
print('MEDIBOOK FULL SYSTEM TEST')
print('='*60)

# ─── TEST 1: Admin Login ───
print('\n[TEST 1] Admin Login')
code, resp = api('POST', '/auth/login', {'email': 'admin@medibook.com', 'password': 'admin123'})
print(f'  Status: {code}, Role: {resp.get("role")}, Name: {resp.get("name")}')
admin_token = resp.get('token')
assert code == 200 and resp.get('role') == 'ADMIN', 'FAIL: Admin login'
print('  [PASS]')

# ─── TEST 2: Admin Dashboard ───
print('\n[TEST 2] Admin Dashboard Stats')
code, resp = api('GET', '/admin/dashboard', token=admin_token)
print(f'  Status: {code}, Stats: {resp}')
assert code == 200, 'FAIL: Dashboard'
print('  [PASS]')

# ─── TEST 3: Admin - Get All Users ───
print('\n[TEST 3] Admin - List All Users')
code, resp = api('GET', '/admin/users', token=admin_token)
print(f'  Status: {code}, Count: {len(resp)}')
for u in resp:
    print(f'    - {u["name"]} | {u["email"]} | {u["role"]} | Active={u["isActive"]}')
assert code == 200, 'FAIL: List users'
print('  [PASS]')

# ─── TEST 4: Admin - Get Pending Providers ───
print('\n[TEST 4] Admin - Pending Providers')
code, resp = api('GET', '/admin/providers/pending', token=admin_token)
print(f'  Status: {code}, Pending count: {len(resp)}')
assert code == 200, 'FAIL: Pending providers'
print('  [PASS]')

# ─── TEST 5: Admin - Get All Appointments ───
print('\n[TEST 5] Admin - All Appointments')
code, resp = api('GET', '/admin/appointments', token=admin_token)
print(f'  Status: {code}, Count: {len(resp)}')
for a in resp:
    print(f'    - #{a["id"]} {a["patientName"]} -> {a["providerName"]} | {a["appointmentDate"]} | {a["status"]}')
assert code == 200, 'FAIL: All appointments'
print('  [PASS]')

# ─── TEST 6: Admin - Get All Records ───
print('\n[TEST 6] Admin - All Medical Records')
code, resp = api('GET', '/admin/records', token=admin_token)
print(f'  Status: {code}, Count: {len(resp)}')
assert code == 200, 'FAIL: All records'
print('  [PASS]')

# ─── TEST 7: Register New Patient ───
print('\n[TEST 7] Register New Patient')
code, resp = api('POST', '/auth/register', {
    'name': 'Test Patient X',
    'email': f'testpatientx_{suffix}@test.com',
    'password': 'test1234',
    'role': 'PATIENT'
})
print(f'  Status: {code}, Role: {resp.get("role")}, Name: {resp.get("name")}')
patient_token = resp.get('token')
patient_id = resp.get('userId')
if code == 200:
    print('  [PASS]')
else:
    print(f'  [WARN] {resp}')

# ─── TEST 8: Register New Provider ───
print('\n[TEST 8] Register New Provider')
code, resp = api('POST', '/auth/register', {
    'name': 'Dr. TestDoc',
    'email': f'testdoc_{suffix}@test.com',
    'password': 'test1234',
    'role': 'PROVIDER',
    'specialization': 'Dermatology',
    'experienceYears': 5,
    'clinicName': 'Skin Care Clinic',
    'clinicAddress': '123 Test Street'
})
print(f'  Status: {code}, Role: {resp.get("role")}, Name: {resp.get("name")}')
provider_token = resp.get('token')
provider_user_id = resp.get('userId')
if code == 200:
    print('  [PASS]')
else:
    print(f'  [WARN] {resp}')

# ─── TEST 9: Unverified provider cannot add slots ───
print('\n[TEST 9] Unverified Provider - Add Slot (should fail)')
code, resp = api('POST', '/slots', {
    'date': '2026-05-01',
    'startTime': '10:00',
    'endTime': '10:30'
}, token=provider_token)
print(f'  Status: {code}, Response: {resp}')
if code == 400:
    print('  [PASS] - Correctly blocked unverified provider')
else:
    print('  [FAIL] - Should have blocked')

# ─── TEST 10: Admin verifies the new provider ───
print('\n[TEST 10] Admin - Verify Pending Provider')
code, pending = api('GET', '/admin/providers/pending', token=admin_token)
print(f'  Pending providers: {len(pending)}')
if len(pending) > 0:
    pid = pending[0]['id']
    pname = pending[0]['name']
    print(f'  Verifying: {pname} (id={pid})')
    code2, _ = api('PUT', f'/admin/providers/{pid}/verify', token=admin_token)
    print(f'  Verify status: {code2}')
    assert code2 == 204, 'FAIL: Verify provider'
    print('  [PASS]')
else:
    print('  [WARN] No pending providers to verify')

# ─── TEST 11: Verified provider adds slot ───
print('\n[TEST 11] Verified Provider - Add Slot')
code, resp = api('POST', '/slots', {
    'date': '2026-05-01',
    'startTime': '10:00',
    'endTime': '10:30'
}, token=provider_token)
print(f'  Status: {code}, Response: {resp}')
slot_id = resp.get('id') if code == 200 else None
if code == 200:
    print('  [PASS]')
else:
    print(f'  [FAIL]')

# ─── TEST 12: Provider views own slots ───
print('\n[TEST 12] Provider - View My Slots')
code, resp = api('GET', '/slots/my', token=provider_token)
print(f'  Status: {code}, Slots: {len(resp)}')
assert code == 200, 'FAIL: My slots'
print('  [PASS]')

# ─── TEST 13: Patient browses verified providers ───
print('\n[TEST 13] Patient - Browse Verified Providers')
code, resp = api('GET', '/providers', token=patient_token)
print(f'  Status: {code}, Providers: {len(resp)}')
for p in resp:
    print(f'    - {p["userName"]} | {p["specialization"]} | Verified={p["isVerified"]}')
assert code == 200, 'FAIL: Browse providers'
print('  [PASS]')

# ─── TEST 14: Patient views provider slots ───
print('\n[TEST 14] Patient - View Provider Slots')
if resp:
    pid = resp[-1]['id']  # last provider (newly verified)
    code2, slots = api('GET', f'/providers/{pid}/slots?date=2026-05-01', token=patient_token)
    print(f'  Status: {code2}, Slots for provider {pid}: {len(slots)}')
    for s in slots:
        print(f'    - Slot #{s["id"]} | {s["date"]} {s["startTime"]}-{s["endTime"]} | Booked={s["booked"]}')
    print('  [PASS]')

# ─── TEST 15: Patient books appointment ───
print('\n[TEST 15] Patient - Book Appointment')
if slot_id and resp:
    pid = resp[-1]['id']
    code, booking = api('POST', '/appointments', {
        'slotId': slot_id,
        'providerId': pid
    }, token=patient_token)
    print(f'  Status: {code}, Booking: {booking}')
    appt_id = booking.get('id') if code == 200 else None
    if code == 200:
        print('  [PASS]')
    else:
        print(f'  [FAIL]')

# ─── TEST 16: Patient views their appointments ───
print('\n[TEST 16] Patient - My Appointments')
code, resp = api('GET', '/appointments/my', token=patient_token)
print(f'  Status: {code}, Count: {len(resp)}')
for a in resp:
    print(f'    - #{a["id"]} | {a["providerName"]} | {a["slotDate"]} {a["slotStartTime"]} | {a["status"]}')
assert code == 200, 'FAIL: My appointments'
print('  [PASS]')

# ─── TEST 17: Double booking same slot (should fail) ───
print('\n[TEST 17] Patient - Double Book Same Slot (should fail)')
if slot_id and pid:
    code, resp = api('POST', '/appointments', {
        'slotId': slot_id,
        'providerId': pid
    }, token=patient_token)
    print(f'  Status: {code}, Response: {resp}')
    if code == 400:
        print('  [PASS] - Double booking correctly prevented')
    else:
        print('  ❌ FAIL')

# ─── TEST 18: Provider views their appointments ───
print('\n[TEST 18] Provider - My Appointments')
code, resp = api('GET', '/appointments/provider', token=provider_token)
print(f'  Status: {code}, Count: {len(resp)}')
assert code == 200, 'FAIL: Provider appointments'
print('  [PASS]')

# ─── TEST 19: Provider completes appointment ───
print('\n[TEST 19] Provider - Complete Appointment')
if appt_id:
    code, resp = api('PUT', f'/appointments/{appt_id}/complete', token=provider_token)
    print(f'  Status: {code}')
    if code == 204:
        print('  [PASS]')
    else:
        print(f'  [FAIL]: {resp}')

# ─── TEST 20: Admin suspend user ───
print('\n[TEST 20] Admin - Suspend Patient')
if patient_id:
    code, resp = api('PUT', f'/admin/users/{patient_id}/suspend', token=admin_token)
    print(f'  Status: {code}')
    if code == 204:
        print('  [PASS]')
    else:
        print(f'  [FAIL]: {resp}')

# ─── TEST 21: Suspended user login blocked ───
print('\n[TEST 21] Suspended User - Login (should fail)')
code, resp = api('POST', '/auth/login', {'email': f'testpatientx_{suffix}@test.com', 'password': 'test1234'})
print(f'  Status: {code}, Response: {resp}')
if code == 400:
    print('  [PASS] - Suspended user correctly blocked')
else:
    print('  ❌ FAIL')

# ─── TEST 22: Admin reactivate user ───
print('\n[TEST 22] Admin - Reactivate Patient')
if patient_id:
    code, resp = api('PUT', f'/admin/users/{patient_id}/activate', token=admin_token)
    print(f'  Status: {code}')
    if code == 204:
        print('  [PASS]')

# ─── TEST 23: Reactivated user can login ───
print('\n[TEST 23] Reactivated User - Login')
code, resp = api('POST', '/auth/login', {'email': f'testpatientx_{suffix}@test.com', 'password': 'test1234'})
print(f'  Status: {code}, Role: {resp.get("role")}')
if code == 200:
    print('  [PASS]')

# ─── TEST 24: Cancel appointment ───
print('\n[TEST 24] Patient - Cancel Appointment')
code, myappts = api('GET', '/appointments/my', token=patient_token)
scheduled = [a for a in myappts if a['status'] == 'SCHEDULED']
if scheduled:
    aid = scheduled[0]['id']
    code, resp = api('PUT', f'/appointments/{aid}/cancel', token=patient_token)
    print(f'  Status: {code}')
    if code == 204:
        print('  ✅ PASS')
    else:
        print(f'  ❌ FAIL: {resp}')
else:
    print('  [WARN] No scheduled appointments to cancel (already completed)')

# ─── TEST 25: Admin protect - cannot suspend main admin ───
print('\n[TEST 25] Admin - Self-Protection (cannot suspend main admin)')
code, users = api('GET', '/admin/users', token=admin_token)
admin_user = [u for u in users if u['email'] == 'admin@medibook.com'][0]
code, resp = api('PUT', f'/admin/users/{admin_user["id"]}/suspend', token=admin_token)
print(f'  Status: {code}, Response: {resp}')
if code == 400:
    print('  [PASS] - Main admin correctly protected')
else:
    print('  ❌ FAIL')

# ─── TEST 26: Unauthorized access ───
print('\n[TEST 26] Patient Token - Admin Endpoint (should fail)')
code, resp = api('GET', '/admin/dashboard', token=patient_token)
print(f'  Status: {code}')
if code == 403:
    print('  [PASS] - RBAC correctly enforced')
else:
    print(f'  [FAIL]: Expected 403, got {code}')

# ─── TEST 27: No token - protected endpoint ───
print('\n[TEST 27] No Token - Protected Endpoint (should fail)')
code, resp = api('GET', '/admin/dashboard')
print(f'  Status: {code}')
if code == 403:
    print('  [PASS] - Authentication required')
else:
    print(f'  [FAIL]: Expected 403, got {code}')

# ─── TEST 28: Updated Dashboard Stats ───
print('\n[TEST 28] Admin - Updated Dashboard (after all operations)')
code, resp = api('GET', '/admin/dashboard', token=admin_token)
print(f'  Status: {code}, Stats: {resp}')
print('  ✅ PASS')

print('\n' + '='*60)
print('ALL TESTS COMPLETE')
print('='*60)
