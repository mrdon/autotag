import speech_recognition as sr
from os import path
import sys


# transcribe audio file                                                         
AUDIO_FILE = sys.argv[1]

# use the audio file as the audio source                                        
r = sr.Recognizer()
with sr.AudioFile(AUDIO_FILE) as source:
        audio = r.record(source)  # read the entire audio file                  

        print("Transcription: " + r.recognize_google(audio))
