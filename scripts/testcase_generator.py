import os
import random 
from Crypto.Cipher import AES

if __name__ == '__main__':
    AES128 = 16
    AES256 = 32
    SIZE_OF_DATA = 1024*1024

    root_path = "data"
    filename_key = "key.bin"
    filename_data = "Input.bin"
    filename_cipher = "cipher.bin"
    filename_decipher = "decipher.bin"
    mode = AES128

    os.makedirs(root_path, mode=511, exist_ok=True)

    random.setstate(random.getstate())
    key = random.randbytes(mode)
    data = random.randbytes(SIZE_OF_DATA)
    cipher = AES.new(key, mode=AES.MODE_ECB)
    ciphertext = cipher.encrypt(data)
    deciphertext = cipher.decrypt(ciphertext)
    with open (os.path.join(root_path, filename_key), "wb") as fptr:
        fptr.write(key)
    with open (os.path.join(root_path, filename_data), "wb") as fptr:
        fptr.write(data)
    with open (os.path.join(root_path, filename_cipher), "wb") as fptr:
        fptr.write(ciphertext)
    with open (os.path.join(root_path, filename_decipher), "wb") as fptr:
        fptr.write(deciphertext)

    if data == deciphertext:
        print("Success!")

    
    exit(0)