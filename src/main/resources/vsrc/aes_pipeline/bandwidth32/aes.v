`include "params.vh"
module AESPipe_BlackBox(
    input clk,
    input reset_n,
    input chip_en,
    input we,
    input [7 : 0] address,
    input [`WIDTH-1:0] datain,
    output [`WIDTH-1:0] dataout
);

localparam IDLE = 0;
localparam KEY = 1;
localparam OPER = 2;

localparam AES128 = 0;
localparam AES192 = 1;
localparam AES256 = 2;

localparam ECB = 0;
/* TO-DO extensions */

localparam ADDR_CTRL = 8'h08;
localparam ADDR_CFG = 8'h0a;
localparam ADDR_KEY_START      = 8'h10;
localparam ADDR_KEY_END        = 8'h17; // modified here temporarily

localparam ENC = 1'b1;
localparam DEC = 1'b0;
integer i;

reg [1:0] st, nxt_st;

reg core_ready;
reg key_ready;
wire key_valid;


reg [1:0] level;
reg [1:0] opmode;
reg encdec;

reg [3:0] key_address;

reg [`WIDTH-1:0] keys [0:`WORDS-1];
reg [255:0] init_key;
wire [127:0] round_key;

reg [31:0] tmp;
always@(*) begin: key_assignment
    for (i = 0; i < 8; i=i+1) begin
        init_key[i*32 +: 32] = keys[i];
    end
end
aes_round_key u_round_key(
    .clk(clk),
    .reset_n(reset_n),
    .rd(0),
    .ready(key_ready),
    .level(level),
    .init_key(init_key),
    .address(key_address),
    .valid(key_valid),
    .round_key(round_key)
);


always@(posedge clk or negedge reset_n) begin: state_machine
    if(!reset_n) begin
        st <= IDLE;
    end
    else begin
        st <= nxt_st;
    end
end
always@(*) begin: state_update
    nxt_st = IDLE;
    case(st)
        IDLE: begin
            if (chip_en) begin
                if (address == ADDR_CTRL) begin
                    if(core_ready == 1'b1)
                        nxt_st = KEY;
                end
            end
        end
        KEY: begin
            nxt_st = KEY;
        end
        default: begin
            nxt_st = IDLE;
        end
    endcase
end

always@(posedge clk or negedge reset_n) begin: reg_update
    if(!reset_n) begin
        core_ready <= 1'b0;
        key_ready <= 1'b0;
        opmode <= ECB;
        level <= AES128;
        encdec <= ENC;
        for(i = 0; i<`WORDS ; i=i+1) begin
            keys[i] <= 0;
        end
    end
    else begin
        if(chip_en) begin
            if (we) begin
                if (address == ADDR_CTRL) begin
                    core_ready <= datain[0];
                    if (st == IDLE)
                        key_ready <= 1'b1;
                    else
                        key_ready <= 1'b0;
                end
                else if (address == ADDR_CFG) begin
                    {encdec, opmode, level} <= datain[4:0];
                end
                else if (address >= ADDR_KEY_START && address <= ADDR_KEY_END)
                    keys[address[2:0]] <= datain;
            end
        end
    end
end





endmodule