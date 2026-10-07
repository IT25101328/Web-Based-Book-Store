import urllib.request
import urllib.error
import json
import uuid

# Register
req = urllib.request.Request('http://localhost:8080/api/auth/register', data=json.dumps({'username': 'testuser3', 'password': 'testuser3', 'email': 'testuser3@a.com'}).encode('utf-8'), headers={'Content-Type': 'application/json'})
try:
    urllib.request.urlopen(req)
except:
    pass

# Login
req = urllib.request.Request('http://localhost:8080/api/auth/login', data=json.dumps({'username': 'testuser3', 'password': 'testuser3'}).encode('utf-8'), headers={'Content-Type': 'application/json'})
resp = urllib.request.urlopen(req)
token = json.loads(resp.read().decode('utf-8'))['token']

# Upload profile picture
boundary = uuid.uuid4().hex
body = (
    '--' + boundary + '\r\n' +
    'Content-Disposition: form-data; name="file"; filename="test.jpg"\r\n' +
    'Content-Type: image/jpeg\r\n\r\n' +
    'fake image data\r\n' +
    '--' + boundary + '--\r\n'
).encode('utf-8')

req = urllib.request.Request('http://localhost:8080/api/user/profile/picture', data=body, headers={
    'Authorization': 'Bearer ' + token,
    'Content-Type': 'multipart/form-data; boundary=' + boundary
})

try:
    resp = urllib.request.urlopen(req)
    print('Status:', resp.getcode())
    print('Body:', resp.read().decode('utf-8'))
except urllib.error.HTTPError as e:
    print('Status:', e.code)
    print('Body:', e.read().decode('utf-8'))
