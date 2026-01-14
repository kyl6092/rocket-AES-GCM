`include "params.vh"
module aes_round_key(
    input clk,
    input reset_n,
    input ready,
    input [1:0] level,
    input [255:0] init_key,
    output reg [1:0] w_valid,
    output reg finish,
    output reg [3:0] address,
    output [255:0] round_key_pre,
    output [255:0] round_key
);

integer i;
reg working;
reg [7:0] rcon, rcon_nxt;
reg [3:0] address_start;
reg [3:0] address_end;

reg [255:0] round_key_buf;


reg [255:0] w_new_combine;
/* verilator lint_off UNOPTFLAT */
reg [31:0] rot_w, last_w, cplx_w;
wire [31:0] last_sw, middle_sw;
reg [31:0] w [0:7];
reg [31:0] w_new [0:7];
reg [31:0] w_reg [0:3];
/* verilator lint_on UNOPTFLAT */

assign round_key_pre = round_key_buf;
assign round_key = w_new_combine;

aes_sbox u_sbox(
    .word(cplx_w),
    .sword(last_sw)
);

always@(posedge clk or negedge reset_n) begin: reg_update
    if (!reset_n) begin
        working <= 1'd0;
        finish <= 1'd0;
        rcon <= 8'd1;
        address <= 0;
        address_start <= 0;
        address_end <= 0;

        w_valid <= 0;

        for (i = 0; i < 4; i=i+1)
            w_reg[i] <= 0;

        round_key_buf <= 0;
    end
    else begin

        if (working) begin
            if (address == address_end) begin
                address <= 0;
                working <= 1'd0;
                finish <= 1'd1;
                w_valid <= 0;
            end
            else begin
                if (level == 2) begin
                    if (w_valid[0] || w_valid[1])
                        w_valid[1] <= ~w_valid[1];
                    w_valid[0] <= ~w_valid[0];
                end
                else if (level == 1) begin
                    if (w_valid[0] || w_valid[1])
                        w_valid[1] <= ~w_valid[1];
                end

                case(w_valid)
                    2'b00: begin
                        w_reg[0] <= w_new[0];
                        w_reg[1] <= w_new[1];
                        w_reg[2] <= w_new[2];
                        w_reg[3] <= w_new[3];
                    end
                    2'b01: begin
                        rcon <= rcon_nxt;
                        round_key_buf <= w_new_combine;
                        if (level == 1 && !w_valid[1])
                            address <= address + 2;
                        else
                            address <= address + 1;
                    end
                    2'b10: begin
                        w_reg[0] <= w_new[0];
                        w_reg[1] <= w_new[1];
                        w_reg[2] <= w_new[2];
                        w_reg[3] <= w_new[3];
                        if (level == 1 && !w_valid[1])
                            address <= address + 2;
                        else
                            address <= address + 1;
                    end
                    2'b11: begin
                        rcon <= rcon_nxt;
                        round_key_buf <= w_new_combine;
                        if (level == 1 && !w_valid[1])
                            address <= address + 2;
                        else
                            address <= address + 1;
                    end
                endcase
            end
        end
        else if (ready) begin
            working <= 1'd1;
            finish <= 1'd0;
            w_valid[0] <= ~(level==2);
            case(level)
                0: begin// AES-128
                    address_start <= 1;
                    address_end <= 10;
                    address <= 1;
                end
                1: begin// AES-192
                    address_start <= 2;
                    address_end <= 12;
                    address <= 2;
                    w_valid[1] <= 1'd1;
                    round_key_buf[191:128] <= init_key[191:128];
                end
                2: begin// AES-256
                    address_start <= 2;
                    address_end <= 14;
                    address <= 2;
                end
            endcase
        end
    end
end

always@(*) begin: sword_select
    if (level == 2) begin
        if (w_valid == 1) begin
            cplx_w = w_reg[3];
        end
        else begin
            cplx_w = rot_w;
        end
    end
    else
        cplx_w = rot_w;
end

always@(*) begin: key_update
    for (i = 0; i < 8; i=i+1) begin
        if(address==address_start) begin
            w[i] = init_key[i*32 +: 32];
        end
        else begin
            w[i] = round_key_buf[i*32 +: 32];
        end
    end

    case(level)
        0: last_w = w[3];
        1: last_w = w[5];
        2: last_w = w[7];
        default: last_w = 0; 
    endcase    

    rot_w = {last_w[23:0], last_w[31:24]};
    w_new[0] = w[0] ^ last_sw ^ {rcon, 24'd0};

    for (i = 1; i < 8; i=i+1) begin
        if (level == 2) begin
            if (i == 4)
                w_new[i] = last_sw ^ w[i];
            else
                w_new[i] = w_new[i-1] ^ w[i];
        end
        else
            w_new[i] = w_new[i-1] ^ w[i];
    end

    for (i = 0; i < 8; i=i+1) begin
        if (level == 2) begin
            if (i >= 4)
                w_new_combine[i*32 +: 32] = w_new[i];
            else
                w_new_combine[i*32 +: 32] = w_reg[i];
        end
        else begin
            w_new_combine[i*32 +: 32] = w_new[i];
        end
    end
end

always@(*) begin
    rcon_nxt = rcon;
    if (working) begin
        if (rcon[7] == 1'b1)
            rcon_nxt = (rcon << 1) ^ 8'b00011011;
        else
            rcon_nxt = rcon << 1;
    end
end


endmodule