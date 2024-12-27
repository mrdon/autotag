import os
import subprocess

from pyftpdlib.servers import FTPServer
from pyftpdlib.authorizers import DummyAuthorizer
from pyftpdlib.handlers import TLS_FTPHandler


class MyHandler(TLS_FTPHandler):
    def on_connect(self):
        print("%s:%s connected" % (self.remote_ip, self.remote_port))

    def on_disconnect(self):
        print("%s:%s disconnected" % (self.remote_ip, self.remote_port))

    def on_login(self, username):
        print("%s:%s login" % (self.remote_ip, self.remote_port))

    def on_logout(self, username):
        print("%s:%s logout" % (self.remote_ip, self.remote_port))

    def on_file_sent(self, file):
        print("%s:%s file sent: %s" % (self.remote_ip, self.remote_port, file))

    def on_file_received(self, file):
        print("%s:%s file received: %s" % (self.remote_ip, self.remote_port, file))

    def on_incomplete_file_sent(self, file):
        print("%s:%s incomplete file sent: %s" % (self.remote_ip, self.remote_port, file))

    def on_incomplete_file_received(self, file):
        print("%s:%s incomplete file received: %s" % (self.remote_ip, self.remote_port, file))


def main():
    pw = os.getenv("USER_PASSWORD")
    assert pw
    authorizer = DummyAuthorizer()
    authorizer.add_user('user', pw, 'web/content/images', perm='elradfmwMT')
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
