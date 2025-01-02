import speech_recognition as sr


def transcribe(voice_file: str) -> str:
    # use the audio file as the audio source
    r = sr.Recognizer()
    with sr.AudioFile(voice_file) as source:
        audio = r.record(source)  # read the entire audio file
        return r.recognize_google(audio)
