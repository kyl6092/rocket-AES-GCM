module aes_sub_bytes (
    input [127:0] state,
    output [127:0] state_sub
);

reg [31:0] round_state_word [0:3];
wire [31:0] round_state_sword [0:3];

integer i;
genvar inst_i;


always@(*) begin
    for (i = 0; i < 4; i=i+1)
        round_state_word[i] = state[32*i +: 32];
end
    
assign state_sub = {round_state_sword[3],round_state_sword[2],round_state_sword[1],round_state_sword[0]};

generate
    for (inst_i = 0; inst_i < 4; inst_i=inst_i+1) begin
        aes_sbox u_sbox(
            .word(round_state_word[inst_i]),
            .sword(round_state_sword[inst_i])
        );
    end
endgenerate



endmodule