module AESGCM_BlackBox(
    input clk,
    input reset_n,
    input chip_en,
    input we,
    input [7 : 0] address,
    input [31:0] datain,
    output reg valid_i,
    output reg valid_o,
    output reg [31:0] dataout
);

localparam IDLE = 0;
localparam KEY = 1;
localparam OPER = 2;

localparam AES128 = 0;
localparam AES192 = 1;
localparam AES256 = 2;

localparam ECB = 0;
localparam CTR = 1;
localparam GCM = 2;
/* TO-DO extensions */

localparam ADDR_CTRL = 8'h08;
localparam ADDR_DATA = 8'h09;
localparam ADDR_CFG = 8'h0a;
localparam ADDR_DATA_END = 8'hb;
localparam ADDR_KEY_START      = 8'h10;
localparam ADDR_KEY_END        = 8'h17;
localparam ADDR_IV_START      = 8'h20;
localparam ADDR_IV_END        = 8'h23;

localparam ENC = 1'b1;
localparam DEC = 1'b0;

integer i;
genvar inst_i, inst_j;

reg [1:0] st, nxt_st;

// System related
reg core_ready;
reg [1:0] cnt, wr_cnt;
reg [1:0] level;
reg [1:0] opmode;
reg encdec;
reg h_reg_flag;

// Key related
reg key_ready;
wire [1:0] key_valid;
wire key_finish;
reg [255:0] init_key;
wire [3:0] key_address;
reg [31:0] keys [0:7];
reg [127:0] round_key_mem [0:14];
wire [255:0] round_key, round_key_pre;
reg [31:0] IV [0:3];
reg [127:0] CB;
wire [127:0] nxt_CB;

// State related
reg [127:0] state;
reg [14:0] round_valid;
reg [127:0] round_state [0:14];
wire [127:0] round_state_sub [0:13];
wire [127:0] round_state_rot [0:13];
wire [127:0] round_state_mix [0:13];
reg [127:0] cipher;
reg [3:0] round_end;
wire [127:0] plaintext;
reg [127:0] ciphertext;

// FIFO_mini
reg push, pop;

// GCM
reg [63:0] cipher_len, additon_len;
wire [127:0] block_len;
reg [63:0] current_len;
reg [1:0] warmup_cnt_wr, warmup_cnt_rd;
reg [127:0] h_reg, c_reg, g_reg, j_reg;
wire [127:0] g;
reg c_ready;
wire g_valid;
reg [127:0] tag;

always@(*) begin: key_assignment
    for (i = 0; i < 8; i=i+1) begin
        init_key[i*32 +: 32] = keys[i];
    end
end
always@(*) begin: CB_assignment
    for (i = 0; i < 4; i=i+1) begin
        CB[i*32 +: 32] = IV[i];
    end
end

assign block_len = {additon_len, cipher_len};

aes_round_key u_round_key(
    .clk(clk),
    .reset_n(reset_n),
    .ready(key_ready),
    .level(level),
    .init_key(init_key),
    .address(key_address),
    .w_valid(key_valid),
    .finish(key_finish),
    .round_key_pre(round_key_pre),
    .round_key(round_key)
);

aes_inc32 u_inc32(
    .in(CB),
    .out(nxt_CB)
);

fifo_mini u_fifo(
    .clk(clk),
    .reset_n(reset_n),
    .push(push),
    .datain(state),
    .pop(pop),
    .dataout(plaintext)
);

// gf128_mul u_gf128_mul(
//     .clk(clk),
//     .rst_n(reset_n),
//     .valid_i  (c_ready),
//     .a_i      (c_reg),
//     .b_i      (h_reg),
//     .valid_o  (g_valid),
//     .result_o (g_reg)
// );

aes_gf128_mul u_gf128_mul(
    .in1(c_reg),
    .in2(h_reg),
    .valid(g_valid),
    .out(g)
);

generate
    for (inst_i = 1; inst_i <= 14; inst_i=inst_i+1) begin
        aes_sub_bytes u_sub_bytes(
            .state(round_state[inst_i]),
            .state_sub(round_state_sub[inst_i-1])
        );

        aes_shift_rows u_shift_rows(
            .state(round_state_sub[inst_i-1]),
            .state_rot(round_state_rot[inst_i-1])
        );

        aes_mix_columns u_mix_columns(
            .state(round_state_rot[inst_i-1]),
            .state_mix(round_state_mix[inst_i-1])
        );
    end
endgenerate


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
            if (key_finish)
                nxt_st = OPER;
            else
                nxt_st = KEY;
        end
        OPER: begin
            nxt_st = OPER;
        end
        default: begin
            nxt_st = IDLE;
        end
    endcase
end

always@(posedge clk or negedge reset_n) begin: reg_update
    if(!reset_n) begin
        // system
        core_ready <= 1'b0;
        key_ready <= 1'b0;
        opmode <= ECB;
        level <= AES128;
        encdec <= ENC;
        cnt <= 0;
        wr_cnt <= 0;
        valid_i <= 0;
        valid_o <= 0;
        // key, state
        state <= 0;
        for(i = 0; i<8 ; i=i+1) begin
            keys[i] <= 0;
        end
        for(i = 0; i<15 ; i=i+1) begin
            round_key_mem[i] <= 0;
            round_state[i] <= 0;
        end
        round_valid <= 0;
        round_end <= 0;
        // CTR
        pop <= 1'b0;
        push <= 1'b0;
        for(i = 0; i<4 ; i=i+1) begin
            IV[i] <= 0;
        end
        // GCM
        warmup_cnt_wr <= 0;
        warmup_cnt_rd <= 0;
        additon_len <= 0;
        cipher_len <= 0;
        current_len <= 0;
        h_reg <= 0;
        h_reg_flag <= 1;
        c_reg <= 0;
        j_reg <= 0;
        g_reg <= 0;
        tag <= 0;
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
                else if (address >= ADDR_IV_START && address <= ADDR_IV_END)
                    IV[address[1:0]] <= datain;
            end
            case(st)
                IDLE: begin
                    case(level)
                        0: begin// AES-128
                            round_key_mem[0] <= init_key[127:0];
                            round_end <= 10;
                        end
                        1: begin// AES-192
                            round_key_mem[0] <= init_key[127:0];
                            round_end <= 12;
                        end
                        2: begin// AES-256
                            round_key_mem[0] <= init_key[127:0];
                            round_key_mem[1] <= init_key[255:128];
                            round_end <= 14;
                        end
                    endcase
                end
                KEY: begin
                    if (key_valid[0]) begin
                        case(level)
                            0,2: begin
                                round_key_mem[key_address] <= round_key[127:0];
                            end
                            1: begin
                                if (key_valid[1]) begin
                                    round_key_mem[key_address] <= round_key[191:64];
                                    round_key_mem[key_address-1] <= {round_key[63:0], round_key_pre[191:128]};
                                end
                                else
                                    round_key_mem[key_address] <= round_key[127:0];
                            end
                        endcase
                    end
                    if (key_valid[1] && level == 2) begin
                        round_key_mem[key_address] <= round_key_pre[255:128];
                    end
                    if (key_finish)
                        valid_i <= 1'b1;
                    if (opmode == GCM) begin
                        warmup_cnt_rd <= 1;
                        warmup_cnt_wr <= 2;
                        IV[3] <= 32'd1; // if len(IV) == 96
                    end
                end
                OPER: begin
                    if (address == ADDR_DATA || address == ADDR_DATA_END) begin
                        if (cnt == 3 && (address == ADDR_DATA || (address == ADDR_DATA_END && (warmup_cnt_wr > 0)))) begin
                            round_valid <= (round_valid << 1) | 15'd1;
                            if (opmode == CTR || opmode == GCM)
                                round_state[0] <= CB;
                            else
                                round_state[0] <= {datain, state[127:32]};

                            state <= {datain, state[127:32]};
                            IV[0] <= nxt_CB[31:0];
                            IV[1] <= nxt_CB[63:32];
                            IV[2] <= nxt_CB[95:64];
                            IV[3] <= nxt_CB[127:96];

                            cnt <= 0;
                            push <= 1;
                            warmup_cnt_wr <= warmup_cnt_wr - 1;
                        end
                        else begin
                            round_valid <= (round_valid << 1);
                            state <= {datain, state[127:32]};
                            cnt <= cnt + 1;
                            push <= 0;
                        end

                        if (round_valid[round_end] == 1'b1 || valid_o) begin
                            wr_cnt <= wr_cnt + 1;
                            if (opmode == CTR || opmode == GCM) begin
                                case(wr_cnt)
                                    0: begin
                                        dataout <= ciphertext[31:0];
                                        valid_o <= round_valid[round_end];
                                    end
                                    1: dataout <= ciphertext[63:32];
                                    2: dataout <= ciphertext[95:64];
                                    3: begin
                                        dataout <= ciphertext[127:96];
                                    end
                                endcase
                            end
                            else begin
                                case(wr_cnt)
                                    0: begin
                                        dataout <= cipher[31:0];
                                        valid_o <= round_valid[round_end];
                                    end
                                    1: dataout <= cipher[63:32];
                                    2: dataout <= cipher[95:64];
                                    3: begin
                                        dataout <= cipher[127:96];
                                    end
                                endcase
                            end
                        end

                        if (round_valid[round_end-2] == 1'b1) 
                            pop <= 1;
                        else
                            pop <= 0;

                        if (round_valid[round_end-3] == 1'b1 && h_reg_flag) begin
                            h_reg <= {cipher[31:0], cipher[63:32], cipher[95:64], cipher[127:96]};
                            h_reg_flag <= 0;
                        end

                        if (round_valid[round_end] == 1'b1) begin
                            if (warmup_cnt_rd != 0)
                                warmup_cnt_rd <= warmup_cnt_rd - 1;
                            case(warmup_cnt_rd)
                                2'd1:
                                    j_reg <= {ciphertext[31:0], ciphertext[63:32], ciphertext[95:64], ciphertext[127:96]};
                                2'd0: begin
                                    c_reg <= g_reg ^ {ciphertext[31:0], ciphertext[63:32], ciphertext[95:64], ciphertext[127:96]};
                                    current_len <= current_len + 128;
                                end
                                2'd3, 2'd2: begin
                                    h_reg <= h_reg;
                                    j_reg <= j_reg;
                                    c_reg <= c_reg;
                                end
                            endcase
                        end

                        if (g_valid)
                            g_reg <= g;
                        if (cipher_len == current_len)
                            // c_reg <= g_reg ^ {block_len[31:0], block_len[63:32], block_len[95:64], block_len[127:96]};
                            c_reg <= g_reg ^ block_len;
                        if (address == ADDR_DATA && cnt == 3)
                            cipher_len <= cipher_len + 128;
                        if ((cipher_len == current_len) && g_valid)
                            tag <= g_reg ^ j_reg;
                            

                        for (i = 0; i < 14; i=i+1) begin
                            if (i==0)
                                round_state[i+1] <= round_state[i] ^ round_key_mem[i];
                            else
                                round_state[i+1] <= round_state_mix[i-1] ^ round_key_mem[i];
                        end
                    end
                end
            endcase
        end
    end
end


always@(*) begin
    cipher = 0;
    case(level)
        0: cipher = round_state_rot[9] ^ round_key_mem[10];
        1: cipher = round_state_rot[11] ^ round_key_mem[12];
        2: cipher = round_state_rot[13] ^ round_key_mem[14];
    endcase
end

always@(*) begin
    ciphertext = plaintext ^ cipher;
end

endmodule