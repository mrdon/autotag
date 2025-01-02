import os
import subprocess
from datetime import datetime

from pyftpdlib.servers import FTPServer
from pyftpdlib.authorizers import DummyAuthorizer
from pyftpdlib.handlers import TLS_FTPHandler


class MyAuthorizer(DummyAuthorizer):

    def get_home_dir(self, username):
        base_dir = super().get_home_dir(username)
        now = datetime.now().strftime('%Y-%m-%d')
        album_dir = os.path.join(base_dir, now)
        if not os.path.isdir(album_dir):
            os.makedirs(album_dir, exist_ok=True)
            now_pretty = datetime.now().strftime('%b %d, %Y')
            with open(os.path.join(album_dir, 'index.md'), 'w') as w:
                w.write(f"""
                ---
title: {now_pretty}
categories: [travel,tech,foo,bar,baz]
---\
                """)
        return album_dir


class MyHandler(TLS_FTPHandler):
    def on_file_received(self, file):
        print("File received: %s" % file)
        # if file.endswith(".JPG"):
        #     print("Processing image: %s" % file)
        #
        #
        # subprocess.run(["python3", "app.py", file])


def main():
    pw = os.getenv("USER_PASSWORD")
    assert pw
    authorizer = MyAuthorizer()
    authorizer.add_user('user', pw, 'web/content/galleries', perm='elradfmwMT')
#    authorizer.add_anonym#ous('.')
    handler = MyHandler
    handler.certfile = 'ftpd.crt'  # <--
    handler.keyfile = 'ftpd.key'  # <--
    handler.authorizer = authorizer
    # optionally require SSL for both control and data channel
    handler.tls_control_required = True
    handler.tls_data_required = True
    handler.passive_ports = (21000, 21010)
    server = FTPServer(('', 2021), handler)
    server.serve_forever()


if __name__ == '__main__':
    main()
