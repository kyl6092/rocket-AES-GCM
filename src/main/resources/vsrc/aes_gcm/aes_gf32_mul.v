module aes_gf32_mul (
    input clk,
    input reset_n,
    input [31:0] in1,
    input [31:0] in2,
    output reg [63:0] out
);


wire [15:0] in1_lo, in1_hi;
wire [15:0] in2_lo, in2_hi;

wire [31:0] z0, z1, z2;

assign in1_lo = in1[15:0];
assign in1_hi = in1[31:16];
assign in2_lo = in2[15:0];
assign in2_hi = in2[31:16];




aes_gf16_mul u0_gf16_mul (
    .clk(clk),
    .reset_n(reset_n),
    .in1(in1_lo),
    .in2(in2_lo),
    .out(z0)
);
aes_gf16_mul u1_gf16_mul (
    .clk(clk),
    .reset_n(reset_n),
    .in1(in1_lo ^ in1_hi),
    .in2(in2_lo ^ in2_hi),
    .out(z1)
);
aes_gf16_mul u2_gf16_mul (
    .clk(clk),
    .reset_n(reset_n),
    .in1(in1_hi),
    .in2(in2_hi),
    .out(z2)
);

always@(posedge clk or negedge reset_n) begin
    if(!reset_n) begin
        out <= 0;
    end
    else begin
        out <= {z2, 32'd0} ^ {16'd0, (z0^z1^z2), 16'd0} ^ {32'd0, z0};
    end
end


endmodule