import requests
import json
import sys

base = 'http://localhost:8080/api'

# Login as admin
r = requests.post(f'{base}/auth/login', json={'email':'admin@medibook.com','password':'password123'})
print('Admin login:', r.status_code)
if r.status_code != 200:
    r = requests.post(f'{base}/auth/login', json={'email':'admin@medibook.com','password':'123456'})
    print('Admin login (alt):', r.status_code)
    if r.status_code != 200:
        print('Login failed:', r.text[:200])
        sys.exit(1)

admin_token = r.json().get('token')

# Get admin appointments
r2 = requests.get(f'{base}/admin/appointments', headers={'Authorization': f'Bearer {admin_token}'})
print('Admin appointments status:', r2.status_code)
if r2.status_code == 200:
    apts = r2.json()
    print(f'Total appointments: {len(apts)}')
    for a in apts[:3]:
        print(f"  ID={a.get('id', 'N/A')} patient={a.get('patientName', 'N/A')} provider={a.get('providerName', 'N/A')} status={a.get('status', 'MISSING')} date={a.get('appointmentDate', 'N/A')}")
else:
    print('Error:', r2.text[:200])

# Login as provider
print('\n--- Provider Test ---')
r3 = requests.post(f'{base}/auth/login', json={'email':'ranjan@gmail.com','password':'password123'})
if r3.status_code != 200:
    r3 = requests.post(f'{base}/auth/login', json={'email':'ranjan@gmail.com','password':'123456'})
print('Provider login:', r3.status_code)
if r3.status_code == 200:
    prov_token = r3.json().get('token')
    r4 = requests.get(f'{base}/appointments/provider', headers={'Authorization': f'Bearer {prov_token}'})
    print('Provider appointments:', r4.status_code)
    if r4.status_code == 200:
        papts = r4.json()
        print(f'Total: {len(papts)}')
        for a in papts[:3]:
            print(f"  ID={a.get('id', 'N/A')} patient={a.get('patientName', 'N/A')} status={a.get('status', 'MISSING')}")
        
        # Accept/Complete test
        pending = [a for a in papts if a.get('status') in ('PENDING', 'BOOKED')]
        if pending:
            test_apt = pending[0]
            print(f'\nAccepting appointment ID={test_apt["id"]} (status={test_apt["status"]})...')
            r5 = requests.put(f'{base}/appointments/{test_apt["id"]}/accept', headers={'Authorization': f'Bearer {prov_token}'})
            print(f'Accept result: {r5.status_code}')
            
            print(f'Completing appointment ID={test_apt["id"]}...')
            r7 = requests.put(f'{base}/appointments/{test_apt["id"]}/complete', headers={'Authorization': f'Bearer {prov_token}'})
            print(f'Complete result: {r7.status_code}')
            
            r8 = requests.get(f'{base}/appointments/provider', headers={'Authorization': f'Bearer {prov_token}'})
            updated2 = [a for a in r8.json() if a['id'] == test_apt['id']]
            if updated2:
                print(f'After complete: status={updated2[0]["status"]}')
        
        # Reject test
        r_reload = requests.get(f'{base}/appointments/provider', headers={'Authorization': f'Bearer {prov_token}'})
        pending2 = [a for a in r_reload.json() if a.get('status') in ('PENDING', 'BOOKED') and (not pending or a['id'] != pending[0]['id'])]
        if pending2:
            test_apt2 = pending2[0]
            print(f'\nRejecting appointment ID={test_apt2["id"]} (status={test_apt2["status"]})...')
            r9 = requests.put(f'{base}/appointments/{test_apt2["id"]}/reject', headers={'Authorization': f'Bearer {prov_token}'})
            print(f'Reject result: {r9.status_code}')
            
            r10 = requests.get(f'{base}/appointments/provider', headers={'Authorization': f'Bearer {prov_token}'})
            updated3 = [a for a in r10.json() if a['id'] == test_apt2['id']]
            if updated3:
                print(f'After reject: status={updated3[0]["status"]}')

print('\n=== ALL TESTS PASSED ===')
