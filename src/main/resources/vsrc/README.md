# Verilog Source (vsrc)

---

-  ### `vsrc/aes_baseline/`
    The verilog code are referenced from [ucb-ee290c/sp21-aes-rocc-accel](https://github.com/ucb-ee290c/sp21-aes-rocc-accel/tree/master). In this repository, we will evaluate the AES implementation and compare it with our High-Performance Pipelined AES-GCM. With [ChiselSim](https://www.chisel-lang.org/docs/explanations/testing), testing framework for the verification and execution cycles can be easily constructed. As for gate count and clock cycle time, we will use the manual  approaches to approximates it. (Here, we don't consider the licensed EDA tools such as design compiler provided the Synopsys.)
    ```bash
    Directory Tree
    aes_baseline/
    ├── aes_core.v
    ├── aes_decipher_block.v
    ├── aes_encipher_block.v
    ├── aes_inv_sbox.v
    ├── aes_key_mem.v
    ├── aes_sbox.v
    └── aes.v
    ```
    ```bash
    Module Tree
    aes.v
    └── aes_core.v
        ├── aes_sbox.v
        ├── aes_key_mem.v
        ├── aes_encipher_block.v
        └── aes_decipher_block.v
            └── aes_inv_sbox.v
    ```
- ### `vsrc/aes_pipeline/`
    The verilog code are modified from [ryanycs/aes](https://github.com/ryanycs/aes/tree/main), which is developed using SystemVerilog and simulated by Synopsys VCS simulator. For this repository, the evaluation and validation are based on open-source tools. To better compare with Chisel language, the HDL is re-written in Verilog and Chisel.
    ```bash
    Directory Tree
    aes_pipeline/
    └── bandwidth32
        ├── aes_mix_columns.v
        ├── aes_round_key.v
        ├── aes_sbox.v
        ├── aes_shift_rows.v
        ├── aes_sub_bytes.v
        └── aes.v
    ```
    ```bash
    Module Tree
    aes.v
    ├── aes_round_key.v
    │   └──aes_sbox.v
    ├── aes_sub_bytes.v
    │   └──aes_sbox.v
    ├── aes_shift_rows.v
    └── aes_mix_columns.v
    ```
- ### `vsrc/aes_ctr/`
    The counter mode in the AES instantiates a new module called `inc32`, which is used to count number for encryption. The AES-CTR will further perform XOR on the encrypted counter number with the input plaintext, producing the ciphertext. Since the input rate and encryption rate are different, so we also add a First-In-First-Out (FIFO) memory to resolve this.
    ```bash
    Directory Tree
    aes_ctr/
    ├── aes_inc32.v
    ├── fifo.v
    ├── aes_mix_columns.v
    ├── aes_round_key.v
    ├── aes_sbox.v
    ├── aes_shift_rows.v
    ├── aes_sub_bytes.v
    └── aes.v
    ```
    ```bash
    Module Tree
    aes.v
    ├── aes_round_key.v
    │   └──aes_sbox.v
    ├── aes_sub_bytes.v
    │   └──aes_sbox.v
    ├── aes_shift_rows.v
    ├── aes_mix_columns.v
    ├── aes_inc32.v
    └── fifo.v
- ### `vsrc/aes_gcm/`
    pending..

---