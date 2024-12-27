FROM python:3.12

ENV PYTHONUNBUFFERED=1

RUN pip install -U pip

WORKDIR /app


COPY requirements.txt .
RUN pip install -r requirements.txt


COPY ftp.py /app
COPY src/dokku/CHECKS /app/CHECKS
COPY src/dokku/nginx.conf.sigil /app/nginx.conf.sigil
COPY Procfile /app
COPY bin /app/bin


#
# ARG REVISION_HASH
# ENV REVISION_HASH ${REVISION_HASH}
# RUN echo "REVISION_HASH '$REVISION_HASH'"

EXPOSE 2021/tcp

CMD ["bin/run-ftp.sh"]
