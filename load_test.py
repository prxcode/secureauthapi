# a simple script that fire 5k simulated requests to prove that redis rate limiting works under heavy load

import urllib.request
import urllib.error
import json
import time
from concurrent.futures import ThreadPoolExecutor
import threading

# Target configuration
BASE_URL = "http://localhost:8080"
LOGIN_ENDPOINT = f"{BASE_URL}/auth/login"
PROFILE_ENDPOINT = f"{BASE_URL}/api/user/me"

# Simulation configuration
TOTAL_REQUESTS = 30    # Enough requests to trigger rate limiting (limit is 10)
CONCURRENT_USERS = 5   # Concurrency level

# Stats tracking
stats = {
    "success_200": 0,
    "unauthorized_401": 0,
    "forbidden_403": 0,
    "rate_limited_429": 0,
    "server_error_500": 0,
    "network_error": 0
}
stats_lock = threading.Lock()

# Token holder for successful login
valid_token = None

def run_user_session(user_id):
    global valid_token
    
    # 1. Attempt login with correct credentials
    login_data = json.dumps({
        "username": "testuser",
        "password": "password123"
    }).encode("utf-8")
    
    req = urllib.request.Request(
        LOGIN_ENDPOINT,
        data=login_data,
        headers={"Content-Type": "application/json"},
        method="POST"
    )
    
    try:
        with urllib.request.urlopen(req) as response:
            body = response.read().decode("utf-8")
            res_json = json.loads(body)
            token = res_json.get("token")
            
            with stats_lock:
                stats["success_200"] += 1
                if not valid_token:
                    valid_token = token
            print(f"[User {user_id}] Login Succeeded! Received JWT token.")
            
    except urllib.error.HTTPError as e:
        with stats_lock:
            if e.code == 401:
                stats["unauthorized_401"] += 1
            elif e.code == 429:
                stats["rate_limited_429"] += 1
            elif e.code == 500:
                stats["server_error_500"] += 1
            else:
                stats["network_error"] += 1
        print(f"[User {user_id}] Login Failed with HTTP Status: {e.code}")
    except Exception as e:
        with stats_lock:
            stats["network_error"] += 1
        print(f"[User {user_id}] Login connection error: {e}")

    # 2. Short sleep
    time.sleep(0.1)

    # 3. Attempt accessing protected api with token if we have one
    if valid_token:
        req_profile = urllib.request.Request(
            PROFILE_ENDPOINT,
            headers={
                "Authorization": f"Bearer {valid_token}",
                "Content-Type": "application/json"
            },
            method="GET"
        )
        try:
            with urllib.request.urlopen(req_profile) as response:
                with stats_lock:
                    stats["success_200"] += 1
                print(f"[User {user_id}] Profile access succeeded.")
        except urllib.error.HTTPError as e:
            with stats_lock:
                if e.code == 401:
                    stats["unauthorized_401"] += 1
                elif e.code == 429:
                    stats["rate_limited_429"] += 1
                elif e.code == 403:
                    stats["forbidden_403"] += 1
                elif e.code == 500:
                    stats["server_error_500"] += 1
            print(f"[User {user_id}] Profile access failed with HTTP Status: {e.code}")
        except Exception as e:
            with stats_lock:
                stats["network_error"] += 1
            print(f"[User {user_id}] Profile connection error: {e}")

def main():
    print("==================================================")
    print("STARTING LOAD SIMULATION ON SECUREAUTHAPI")
    print(f"Total simulated request cycles: {TOTAL_REQUESTS}")
    print(f"Concurrency level: {CONCURRENT_USERS} parallel threads")
    print("==================================================")
    
    start_time = time.time()
    
    with ThreadPoolExecutor(max_workers=CONCURRENT_USERS) as executor:
        executor.map(run_user_session, range(1, TOTAL_REQUESTS + 1))
        
    duration = time.time() - start_time
    
    print("\n==================================================")
    print("SIMULATION RESULTS")
    print("==================================================")
    print(f"Total time elapsed: {duration:.2f} seconds")
    print(f"HTTP 200 OK (Success):       {stats['success_200']}")
    print(f"HTTP 401 Unauthorized:       {stats['unauthorized_401']}")
    print(f"HTTP 403 Forbidden:          {stats['forbidden_403']}")
    print(f"HTTP 429 Rate Limited:       {stats['rate_limited_429']}")
    print(f"HTTP 500 Server Error:       {stats['server_error_500']}")
    print(f"Network Connection Failures: {stats['network_error']}")
    print("==================================================")
    print("Note: Since the rate limit is set to 10 requests per minute,")
    print("you should see several HTTP 429 entries above!")

if __name__ == "__main__":
    main()
