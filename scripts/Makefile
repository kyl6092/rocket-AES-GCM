
# SIZE := 1048576
SIZE := 1024

AES128_ECB: testcase_generator.py
	python testcase_generator.py --mode "ECB" --keylen 16 --size ${SIZE}
AES128_CTR: testcase_generator.py
	python testcase_generator.py --mode "CTR" --keylen 16 --size ${SIZE}

AES192_ECB: testcase_generator.py
	python testcase_generator.py --mode "ECB" --keylen 24 --size ${SIZE}
AES192_CTR: testcase_generator.py
	python testcase_generator.py --mode "CTR" --keylen 24 --size ${SIZE}

AES256_ECB: testcase_generator.py
	python testcase_generator.py --mode "ECB" --keylen 32 --size ${SIZE}
AES256_CTR: testcase_generator.py
	python testcase_generator.py --mode "CTR" --keylen 32 --size ${SIZE}

AES128_ECB_man: testcase_generator.py
	python testcase_generator.py --man --key "2b7e151628aed2a6abf7158809cf4f3c" --data "3243f6a8885a308d313198a2e0370734" --keylen 16 --size 16 --mode "ECB"
AES128_CTR_man: testcase_generator.py
	python testcase_generator.py --man --key "2b7e151628aed2a6abf7158809cf4f3c" --data "6bc1bee22e409f96e93d7e117393172a" --IV "f0f1f2f3f4f5f6f7f8f9fafbfcfdfeff" --keylen 16 --size 16 --mode "CTR"

AES192_ECB_man: testcase_generator.py
	python testcase_generator.py --man --key "8e73b0f7da0e6452c810f32b809079e562f8ead2522c6b7b" --data "3243f6a8885a308d313198a2e0370734" --keylen 24 --size 16 --mode "ECB"	
AES192_CTR_man: testcase_generator.py
	python testcase_generator.py --man --key "8e73b0f7da0e6452c810f32b809079e562f8ead2522c6b7b" --data "6bc1bee22e409f96e93d7e117393172a" --IV "f0f1f2f3f4f5f6f7f8f9fafbfcfdfeff" --keylen 16 --size 16 --mode "CTR"

AES256_ECB_man: testcase_generator.py
	python testcase_generator.py --man --key "603deb1015ca71be2b73aef0857d77811f352c073b6108d72d9810a30914dff4" --data "3243f6a8885a308d313198a2e0370734" --keylen 32 --size 16 --mode "ECB"
AES256_CTR_man: testcase_generator.py
	python testcase_generator.py --man --key "603deb1015ca71be2b73aef0857d77811f352c073b6108d72d9810a30914dff4" --data "6bc1bee22e409f96e93d7e117393172a" --IV "f0f1f2f3f4f5f6f7f8f9fafbfcfdfeff" --keylen 16 --size 16 --mode "CTR"

update:
	cp data/*.bin ../src/test/resources/.
clean:
	rm -rf data/
