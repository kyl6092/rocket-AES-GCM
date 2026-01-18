
# SIZE := 1048576
SIZE := 1024

AES128_ECB: testcase_generator.py
	python testcase_generator.py --mode "ECB" --keylen 16 --size ${SIZE}
AES128_CTR: testcase_generator.py
	python testcase_generator.py --mode "CTR" --keylen 16 --size ${SIZE}
AES128_GCM: testcase_generator.py
	python testcase_generator.py --mode "GCM" --keylen 16 --size ${SIZE}

AES192_ECB: testcase_generator.py
	python testcase_generator.py --mode "ECB" --keylen 24 --size ${SIZE}
AES192_CTR: testcase_generator.py
	python testcase_generator.py --mode "CTR" --keylen 24 --size ${SIZE}
AES192_GCM: testcase_generator.py
	python testcase_generator.py --mode "GCM" --keylen 24 --size ${SIZE}

AES256_ECB: testcase_generator.py
	python testcase_generator.py --mode "ECB" --keylen 32 --size ${SIZE}
AES256_CTR: testcase_generator.py
	python testcase_generator.py --mode "CTR" --keylen 32 --size ${SIZE}
AES256_GCM: testcase_generator.py
	python testcase_generator.py --mode "GCM" --keylen 32 --size ${SIZE}

AES128_ECB_man: testcase_generator.py
	python testcase_generator.py --man --key "2b7e151628aed2a6abf7158809cf4f3c" --data "3243f6a8885a308d313198a2e0370734" --keylen 16 --size 16 --mode "ECB"
AES128_CTR_man: testcase_generator.py
	python testcase_generator.py --man --key "2b7e151628aed2a6abf7158809cf4f3c" --data "6bc1bee22e409f96e93d7e117393172a" --IV "f0f1f2f3f4f5f6f7f8f9fafbfcfdfeff" --keylen 16 --size 16 --mode "CTR"
AES128_GCM_man: testcase_generator.py
	python testcase_generator.py --man --key "00000000000000000000000000000000" --data "00000000000000000000000000000000" --IV "000000000000000000000000" --keylen 16 --size 16 --mode "GCM"
# 	python testcase_generator.py --man --key "00000000000000000000000000000000" --data "00000000000000000000000000000000" --IV "00000000000000000000000000000000" --keylen 16 --size 16 --mode "GCM"

AES192_ECB_man: testcase_generator.py
	python testcase_generator.py --man --key "8e73b0f7da0e6452c810f32b809079e562f8ead2522c6b7b" --data "3243f6a8885a308d313198a2e0370734" --keylen 24 --size 16 --mode "ECB"	
AES192_CTR_man: testcase_generator.py
	python testcase_generator.py --man --key "8e73b0f7da0e6452c810f32b809079e562f8ead2522c6b7b" --data "6bc1bee22e409f96e93d7e117393172a" --IV "f0f1f2f3f4f5f6f7f8f9fafbfcfdfeff" --keylen 24 --size 16 --mode "CTR"
AES192_GCM_man: testcase_generator.py
	python testcase_generator.py --man --key "000000000000000000000000000000000000000000000000" --data "00000000000000000000000000000000" --IV "000000000000000000000000" --keylen 24 --size 16 --mode "GCM"
# 	python testcase_generator.py --man --key "000000000000000000000000000000000000000000000000" --data "00000000000000000000000000000000" --IV "00000000000000000000000000000000" --keylen 24 --size 16 --mode "GCM"

AES256_ECB_man: testcase_generator.py
	python testcase_generator.py --man --key "603deb1015ca71be2b73aef0857d77811f352c073b6108d72d9810a30914dff4" --data "3243f6a8885a308d313198a2e0370734" --keylen 32 --size 16 --mode "ECB"
AES256_CTR_man: testcase_generator.py
	python testcase_generator.py --man --key "603deb1015ca71be2b73aef0857d77811f352c073b6108d72d9810a30914dff4" --data "6bc1bee22e409f96e93d7e117393172a" --IV "f0f1f2f3f4f5f6f7f8f9fafbfcfdfeff" --keylen 32 --size 16 --mode "CTR"
AES256_GCM_man: testcase_generator.py
	python testcase_generator.py --man --key "0000000000000000000000000000000000000000000000000000000000000000" --data "00000000000000000000000000000000" --IV "000000000000000000000000" --keylen 32 --size 16 --mode "GCM"
# 	python testcase_generator.py --man --key "0000000000000000000000000000000000000000000000000000000000000000" --data "00000000000000000000000000000000" --IV "00000000000000000000000000000000" --keylen 32 --size 16 --mode "GCM"

update_ECB:
	cp data/Input.bin 		../src/test/resources/ECB/.
	cp data/key.bin 		../src/test/resources/ECB/.
	cp data/cipher.bin 		../src/test/resources/ECB/.
	cp data/decipher.bin 	../src/test/resources/ECB/.
update_CTR:
	cp data/Input.bin 		../src/test/resources/CTR/.
	cp data/key.bin 		../src/test/resources/CTR/.
	cp data/cipher.bin 		../src/test/resources/CTR/.
	cp data/decipher.bin 	../src/test/resources/CTR/.
	cp data/iv.bin			../src/test/resources/CTR/.
update_GCM:
	cp data/Input.bin 		../src/test/resources/GCM/.
	cp data/key.bin 		../src/test/resources/GCM/.
	cp data/cipher.bin 		../src/test/resources/GCM/.
	cp data/decipher.bin 	../src/test/resources/GCM/.
	cp data/iv.bin			../src/test/resources/GCM/.
	cp data/tag.bin			../src/test/resources/GCM/.
clean:
	rm -rf data/
