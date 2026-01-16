module aes_mix_columns (
    input [127:0] state,
    output reg [127:0] state_mix
);

reg [7:0] s [0:15];

reg [7:0] s_mix [0:15];

integer i;
genvar inst_i;

function [7:0] times2;
    input [7:0] in;
    begin
        if (in[7] == 1'b1)
            times2 = (in << 1) ^ 8'b00011011;
        else
            times2 = (in << 1);
    end
endfunction

function [7:0] times3;
    input [7:0] in;
    begin
        times3 = times2(in) ^ in;
    end
endfunction

always@(*) begin
    for (i = 0; i < 16; i=i+1) begin
        s[i] = state[i*8 +: 8];
    end

    for (i = 0; i < 4; i=i+1) begin
        s_mix[4*i+3] = times2(s[4*i+3]) ^ times3(s[4*i+2]) ^ s[4*i+1] ^ s[4*i];
        s_mix[4*i+2] = times2(s[4*i+2]) ^ times3(s[4*i+1]) ^ s[4*i+3] ^ s[4*i];
        s_mix[4*i+1] = times2(s[4*i+1]) ^ times3(s[4*i]) ^ s[4*i+3] ^ s[4*i+2];
        s_mix[4*i] = times2(s[4*i]) ^ times3(s[4*i+3]) ^ s[4*i+2] ^ s[4*i+1];
    end

    state_mix = {s_mix[15], s_mix[14], s_mix[13], s_mix[12], s_mix[11], s_mix[10], s_mix[9], s_mix[8], s_mix[7], s_mix[6], s_mix[5], s_mix[4], s_mix[3], s_mix[2], s_mix[1], s_mix[0]};
end

    
endmodule