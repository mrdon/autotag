from dotenv import load_dotenv

load_dotenv()

import os

from pyftpdlib.servers import FTPServer
from pyftpdlib.authorizers import DummyAuthorizer
from pyftpdlib.handlers import TLS_FTPHandler

from autotag.ai import run_photo_agent
from autotag.albums import create_album, regenerate_site
from autotag.voice import transcribe


class MyAuthorizer(DummyAuthorizer):

    def get_home_dir(self, username):
        base_dir = super().get_home_dir(username)
        return create_album(base_dir)


class MyHandler(TLS_FTPHandler):
    def on_file_received(self, file):
        print("File received: %s" % file)
        if any(ext for ext in (".JPG", ".jpg", ".jpeg", ".png", ".PNG", ".webp") if file.endswith(ext)):
            print("Rebuilding site")
            regenerate_site()
        elif file.endswith(".WAV"):
            print("Transcribing audio")
            translation = transcribe(file)
            print("Transcription: " + translation)
            result = run_photo_agent(os.getenv("BASEURL"), file[:-4] + ".JPG", translation)
            print("Result: " + result)
            os.remove(file)


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
