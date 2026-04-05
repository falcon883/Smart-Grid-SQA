#!/usr/bin/env python3
"""
serve.py — Energy Monitor Dashboard Mock Server
Serves the HTML pages on routes matching BaseTest.java BASE_URL = http://localhost:3000

Usage:
    python serve.py          # runs on port 3000 (matches BaseTest.java)
    python serve.py 8080     # alternate port
"""

import sys
import os
from http.server import HTTPServer, BaseHTTPRequestHandler

PORT = int(sys.argv[1]) if len(sys.argv) > 1 else 3000
WEBAPP_DIR = os.path.join(os.path.dirname(__file__), 'webapp')

ROUTES = {
    '/':           'dashboard.html',
    '/login':      'login.html',
    '/dashboard':  'dashboard.html',
    '/alerts':     'alerts.html',
    '/settings':   'settings.html',
}

class Handler(BaseHTTPRequestHandler):
    def do_GET(self):
        path = self.path.split('?')[0].rstrip('/')
        if not path:
            path = '/dashboard'

        filename = ROUTES.get(path)
        if not filename:
            self.send_response(404)
            self.end_headers()
            self.wfile.write(b'404 Not Found')
            return

        filepath = os.path.join(WEBAPP_DIR, filename)
        if not os.path.exists(filepath):
            self.send_response(404)
            self.end_headers()
            self.wfile.write(f'File not found: {filepath}'.encode())
            return

        with open(filepath, 'rb') as f:
            content = f.read()

        self.send_response(200)
        self.send_header('Content-Type', 'text/html; charset=utf-8')
        self.send_header('Content-Length', str(len(content)))
        self.end_headers()
        self.wfile.write(content)

    def log_message(self, format, *args):
        print(f'[SmartGrid Server] {self.address_string()} - {format % args}')


if __name__ == '__main__':
    server = HTTPServer(('localhost', PORT), Handler)
    print(f'✅  Energy Monitor running at http://localhost:{PORT}')
    print(f'    Routes: /login  /dashboard  /alerts  /settings')
    print(f'    Press Ctrl+C to stop\n')
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print('\nServer stopped.')
