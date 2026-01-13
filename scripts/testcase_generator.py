import os
import random 
from Crypto.Cipher import AES
import argparse

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument("--mode", help="Select Operation Mode", type=str)
    parser.add_argument("--key", help="Specify key", type=str)
    parser.add_argument("--keylen", help="Specify key length", type=int)
    parser.add_argument("--data", help="Specify data", type=str)
    parser.add_argument("--size", help="Size of data", type=int)
    parser.add_argument("--man", help="The flag for manually entering the testcase", action="store_true")
    args = parser.parse_args()
    
    
    KEY_LEN = args.keylen; assert (KEY_LEN is not None) and (KEY_LEN in {16, 24, 32})
    SIZE_OF_DATA = args.size; assert ((SIZE_OF_DATA is not None) and (SIZE_OF_DATA % 16 == 0))
    MODE = args.mode; assert MODE in {"ECB", "CTR"} # implement later
    mode_list = {"ECB": AES.MODE_ECB, "CTR": AES.MODE_CTR}
    opmode = mode_list[MODE]

    root_path = "data"
    filename_key = "key.bin"
    filename_data = "Input.bin"
    filename_cipher = "cipher.bin"
    filename_decipher = "decipher.bin"
    os.makedirs(root_path, mode=511, exist_ok=True)

    if args.man:
        key = bytes.fromhex(args.key)
        data = bytes.fromhex(args.data)
        print(key)
    else:
        random.setstate(random.getstate())
        key = random.randbytes(KEY_LEN)
        data = random.randbytes(SIZE_OF_DATA)

    cipher = AES.new(key, mode=opmode)
    ciphertext = cipher.encrypt(data)
    deciphertext = cipher.decrypt(ciphertext)
    
    if data == deciphertext:
        print("Success!")
        with open (os.path.join(root_path, filename_key), "wb") as fptr:
            fptr.write(key)
        with open (os.path.join(root_path, filename_data), "wb") as fptr:
            fptr.write(data)
        with open (os.path.join(root_path, filename_cipher), "wb") as fptr:
            fptr.write(ciphertext)
        with open (os.path.join(root_path, filename_decipher), "wb") as fptr:
            fptr.write(deciphertext)

    
    exit(0)