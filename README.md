# KEYRYX

**Every node has a voice.**

KEYRYX is a Java Swing-based LAN chat application designed for communication between users connected to the same local network. It supports account-based access, real-time messaging, and file sharing through a separate transfer service.

## Features

- **One-to-one and group messaging** between connected users.
- **User accounts** with administrator approval for newly registered accounts.
- **Administrator access** for managing pending account approvals.
- **Online user visibility** and connection status.
- **Message timestamps** and persistent chat history.
- **File sharing** between connected users.
- **Desktop notifications** for incoming activity.
- **Java Swing desktop interface** with an About KEYRYX section.

## Download

The latest Windows installer is **KEYRYX 1.0.3**.

- [Download KEYRYX 1.0.3 for Windows](https://github.com/kmsadmansakib2005-rgb/KEYRYX/blob/main/installer/KEYRYX-1.0.3.exe)
- [Browse all available installers](https://github.com/kmsadmansakib2005-rgb/KEYRYX/tree/main/installer)

The installer creates Start Menu and Desktop shortcuts. The installer packages the desktop client; a KEYRYX server must also be running on the local network for clients to connect.

## Requirements

### Desktop client

- Windows 10 or Windows 11.
- Use the installer above. The packaged application includes its Java runtime.

### Server

- Java Development Kit (JDK) 11 or newer.
- A Windows computer on the same LAN as the clients.
- TCP ports **1000** (chat/authentication) and **1001** (file transfer) available through the host's firewall.

## Run the server from source (Windows)

1. Clone or download this repository.
2. Open PowerShell in the repository folder.
3. Compile the Java source files:

   ```powershell
   New-Item -ItemType Directory -Force .\out | Out-Null

   $sources = Get-ChildItem .\src -Recurse -Filter *.java |
       ForEach-Object { $_.FullName }

   javac -encoding UTF-8 -d .\out $sources
   ```

4. Copy the GUI resources into the compiled output:

   ```powershell
   Copy-Item .\src\gui\*.png .\out\gui\
   Copy-Item .\src\gui\*.jpg .\out\gui\
   Copy-Item .\src\gui\*.jpeg .\out\gui\
   ```

5. Start the server:

   ```powershell
   java -cp .\out server.Server
   ```

On the first start, the server asks you to create the administrator account. Keep the server terminal running while clients are connected.

## Connect desktop clients

1. Make sure the server computer and client computers are connected to the same local network.
2. Find the server computer's local IPv4 address (for example, with `ipconfig` on Windows).
3. Launch KEYRYX on each client computer.
4. Enter the server's IPv4 address when prompted.
5. Choose **Create account** to register, or **Sign in** if the account is already approved.
6. A newly registered account remains pending until an administrator approves it.

For testing on the same computer as the server, enter `127.0.0.1` as the server address.

### Account requirements

| Account type | Username | Password |
|---|---|---|
| Regular user | 3–20 letters, numbers, or underscores | At least 10 characters |
| Administrator | 3–20 letters, numbers, or underscores | At least 12 characters |

## Network and data notes

- Allow inbound TCP traffic on ports **1000** and **1001** on the server computer.
- Keep the server running while users are chatting or transferring files.
- Received files are saved under the user's `Documents\KEYRYX\received_files` folder.
- The server stores account and administrator data in `users.db` and `admin.db`, and chat history in `chat_history.txt` in its working directory. Keep these files private and back them up securely.
- KEYRYX is intended for trusted local networks. Do not expose its server ports directly to the public internet.

## Project structure

```text
KEYRYX/
├── installer/       # Windows installers
└── src/
    ├── client/      # Client networking and file transfer
    ├── gui/         # Java Swing desktop interface and image resources
    ├── model/       # Message and user models
    ├── notification/# Desktop notification classes
    ├── security/    # Authentication, password hashing, and sessions
    └── server/      # Chat server, client handlers, and file transfer server
```

## Technology

- Java
- Java Swing
- Java networking (TCP sockets)
- Multithreading
- Java security and password hashing

## Developer

**K. M. Sadman Sakib**

Built with Java Swing.

---

*KEYRYX — Every node has a voice.*
