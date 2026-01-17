import os
import random 
from Crypto.Cipher import AES
from Crypto.Util import Counter
import argparse

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument("--mode", help="Select Operation Mode", type=str)
    parser.add_argument("--key", help="Specify key", type=str)
    parser.add_argument("--keylen", help="Specify key length", type=int)
    parser.add_argument("--data", help="Specify data", type=str)
    parser.add_argument("--size", help="Size of data", type=int)
    parser.add_argument("--man", help="The flag for manually entering the testcase", action="store_true")
    parser.add_argument("--IV", help="The initial vector used for AES-CTR", type=str)
    args = parser.parse_args()
    
    KEY_LEN = args.keylen; assert (KEY_LEN is not None) and (KEY_LEN in {16, 24, 32})
    SIZE_OF_DATA = args.size; assert ((SIZE_OF_DATA is not None) and (SIZE_OF_DATA % 16 == 0))
    MODE = args.mode; assert MODE in {"ECB", "CTR", "GCM"} # implement later
    mode_list = {"ECB": AES.MODE_ECB, "CTR": AES.MODE_CTR, "GCM": AES.MODE_GCM}
    opmode = mode_list[MODE]

    root_path = "data"
    filename_key = "key.bin"
    filename_data = "Input.bin"
    filename_cipher = "cipher.bin"
    filename_decipher = "decipher.bin"
    filename_iv = "iv.bin"
    filename_tag = "tag.bin"
    os.makedirs(root_path, mode=511, exist_ok=True)

    if args.man:
        key = bytes.fromhex(args.key)
        data = bytes.fromhex(args.data)
        if args.mode == "CTR":
            assert args.IV is not None
            iv_bytes = bytes.fromhex(args.IV)
            iv = int(args.IV, 16)
            ctr = Counter.new(128, initial_value=iv)
            cipher = AES.new(key, mode=opmode, counter=ctr)
        elif args.mode == 'GCM':
            assert args.IV is not None
            iv_bytes = bytes.fromhex(args.IV)
            cipher = AES.new(key, mode=opmode, nonce=iv_bytes)
        else:
            cipher = AES.new(key, mode=opmode)
    else:
        random.setstate(random.getstate())
        key = random.randbytes(KEY_LEN)
        data = random.randbytes(SIZE_OF_DATA)
        if args.mode == "CTR":
            iv_bytes = random.randbytes(16)
            iv = int.from_bytes(iv_bytes)
            ctr = Counter.new(128, initial_value=iv)
            cipher = AES.new(key, mode=opmode, counter=ctr)
        elif args.mode == 'GCM':
            iv_bytes = random.randbytes(16)
            cipher = AES.new(key, mode=opmode, nonce=iv_bytes)
        else:
            cipher = AES.new(key, mode=opmode)

    if args.mode == "CTR":
        ciphertext = cipher.encrypt(data)
        decipher = AES.new(key, mode=opmode, counter=ctr)
        deciphertext = decipher.decrypt(ciphertext)
    elif args.mode == "GCM":
        ciphertext, tag = cipher.encrypt_and_digest(data)
        nonce = cipher.nonce
        decipher = AES.new(key, mode=opmode, nonce=nonce)
        deciphertext = decipher.decrypt(ciphertext)
    else:
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
        if args.mode == "CTR" or args.mode == "GCM":
            with open (os.path.join(root_path, filename_iv), "wb") as fptr:
                fptr.write(iv_bytes)
        if args.mode == "GCM":
            with open (os.path.join(root_path, filename_tag), "wb") as fptr:
                fptr.write(tag)

    
    exit(0)