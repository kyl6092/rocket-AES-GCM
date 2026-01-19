module aes_ghash(
    input clk,
    input reset_n,
    input [127:0] in1,
    input [127:0] in2,
    output reg [127:0] out
);


reg [127:0] in1_br;
reg [127:0] in2_br;
wire [127:0] out_br;
wire [255:0] tmp;
integer i;

always@(*) begin
    for (i = 0; i < 128; i = i+1) begin
        in1_br[i] = in1[127-i];
        in2_br[i] = in2[127-i];
    end
end

aes_gf128_mul u_gf128_mul(
    .clk(clk),
    .reset_n(reset_n),
    .in1(in1_br),
    .in2(in2_br),
    .out(tmp)
);


aes_gf128_reduce u_gf128_reduce(
    .clk(clk),
    .reset_n(reset_n),
    .in(tmp),
    .out(out_br)
);

always@(*) begin
    for (i = 0; i < 128; i = i+1) begin
        out[i] = out_br[127-i];
    end
end

endmodule;