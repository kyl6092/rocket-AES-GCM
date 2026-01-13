`define WIDTH 32
`define WORDS 8 // 256/32
// `define AES192
/*
|         | Key length   | Block size   | Number of rounds |
|:-------:| ------------ | ------------ |:----------------:|
|         | Nk (in bits) | Nb (in bits) |        Nr        |
| AES-128 | 4 (128-bit)  | 4 (128-bit)  |        10        |
| AES-192 | 6 (192-bit)  | 4 (128-bit)  |        12        |
| AES-256 | 8 (256-bit)  | 4 (128-bit)  |        14        |
*/

// AES-128
`ifdef AES128
`define KEYLEN 128
`define Nr 10
`define Nb 4
`define Nk 4
`endif

// AES-192
`ifdef AES192
`define KEYLEN 192
`define Nr 12
`define Nb 4
`define Nk 6
`endif


// AES-256
`ifdef AES256
`define KEYLEN 256
`define Nr 14
`define Nb 4
`define Nk 8
`endif