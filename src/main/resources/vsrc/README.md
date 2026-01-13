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
    The verilog code are modified from [ryanycs/aes](https://github.com/ryanycs/aes/tree/main), which is developed using SystemVerilog and simulated by Synopsys VCS simulator. For this repository, the evaluation and validation are based on open-source tools. To better compare with Chisel language, the HDL is re-written in Verilog.

- ### example/