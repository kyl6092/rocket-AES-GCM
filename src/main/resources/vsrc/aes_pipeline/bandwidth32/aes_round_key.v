`include "params.vh"
module aes_round_key(
    input clk,
    input reset_n,
    input ready,
    input [1:0] level,
    input [255:0] init_key,
    input rd,
    input [3:0] address,
    output reg valid,
    output reg [127:0] round_key
);

integer i;
reg working;
reg wr;
reg [1:0] wr_pos;
reg [7:0] rcon, rcon_nxt;
reg [3:0] address_internal;
reg [3:0] address_out;
reg [3:0] address_start;
reg [3:0] address_end;

reg [255:0] round_key_buf;
reg [127:0] round_key_remain;
reg [127:0] round_key_mem [0:14];


reg [255:0] w_new_combine;
/* verilator lint_off UNOPTFLAT */
reg [31:0] rot_w, last_w, cplx_w;
wire [31:0] last_sw, middle_sw;
reg [31:0] w [0:7];
reg [31:0] w_new [0:7];
reg [31:0] w_reg [0:3];
reg w_valid;
/* verilator lint_on UNOPTFLAT */


aes_sbox u_sbox(
    .word(cplx_w),
    .sword(last_sw)
);

always@(posedge clk or negedge reset_n) begin: reg_update
    if (!reset_n) begin
        working <= 1'd0;
        wr <= 1'd0;
        wr_pos <= 2'd0;
        valid <= 1'd0;
        rcon <= 8'd1;
        round_key <= 0;
        address_internal <= 0;
        address_start <= 0;
        address_end <= 0;

        w_valid <= 0;

        for (i = 0; i < 14; i=i+1)
            round_key_mem[i] <= 0;
        for (i = 0; i < 4; i=i+1)
            w_reg[i] <= 0;

        round_key_buf <= 0;
        round_key_remain <= 0;
    end
    else begin
        if (ready) begin
            working <= 1'd1;
            valid <= 1'd0;
            w_valid <= ~(level==2);
            case(level)
                0: begin// AES-128
                    address_start <= 1;
                    address_end <= 10;
                    address_internal <= 1;
                    round_key_mem[0] <= init_key[127:0];
                end
                1: begin// AES-192
                    address_start <= 2;
                    address_end <= 12;
                    address_internal <= 2;
                    wr <= 1'd1;
                    round_key_mem[0] <= init_key[127:0];
                    round_key_buf[191:128] <= init_key[191:128];
                end
                2: begin// AES-256
                    address_start <= 2;
                    address_end <= 14;
                    address_internal <= 2;
                    round_key_mem[0] <= init_key[127:0];
                    round_key_mem[1] <= init_key[255:128];
                end
            endcase
        end

        if (working) begin
            if (address_internal == address_end) begin
                address_internal <= 0;
                working <= 1'd0;
                valid <= 1'd1;
            end
            else begin
                if (w_valid || wr)
                    if (level == 1 && !wr)
                        address_internal <= address_internal + 2;
                    else
                        address_internal <= address_internal + 1;
            end

            if (level == 2)
                w_valid <= ~w_valid;

            if (w_valid) begin
                wr <= ~wr;
                rcon <= rcon_nxt;
                round_key_buf <= w_new_combine;
                case(level)
                    0,2: begin
                        round_key_mem[address_internal] <= w_new_combine[127:0];
                    end
                    1: begin
                        if (wr) begin
                            round_key_mem[address_internal] <= w_new_combine[191:64];
                            round_key_mem[address_internal-1] <= {w_new_combine[63:0], round_key_buf[191:128]};
                        end
                        else
                            round_key_mem[address_internal] <= w_new_combine[127:0];
                    end
                endcase
            end
            else begin
                w_reg[0] <= w_new[0];
                w_reg[1] <= w_new[1];
                w_reg[2] <= w_new[2];
                w_reg[3] <= w_new[3];
            end
            if (wr && level == 2) begin
                wr <= ~wr;
                round_key_mem[address_internal] <= round_key_buf[255:128];
            end

        end
    end
end

always@(*) begin: address_select
    if (rd) begin
        address_out = address;
    end
    else begin
        address_out = address_internal;
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
        if(address_internal==address_start) begin
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
        if (address_internal == 4'd7)
            rcon_nxt = (rcon << 1) ^ 8'b00011011;
        else
            rcon_nxt = rcon << 1;
    end
end


endmodule