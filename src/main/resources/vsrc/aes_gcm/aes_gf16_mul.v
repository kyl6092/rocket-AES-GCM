module aes_gf16_mul (
    input clk,
    input reset_n,
    input [15:0] in1,
    input [15:0] in2,
    output [31:0] out
);


wire [7:0] in1_lo, in1_hi;
wire [7:0] in2_lo, in2_hi;

wire [15:0] z0, z1, z2;

assign in1_lo = in1[7:0];
assign in1_hi = in1[15:8];
assign in2_lo = in2[7:0];
assign in2_hi = in2[15:8];


aes_gf8_mul u0_gf8_mul (
    .clk(clk),
    .reset_n(reset_n),
    .in1(in1_lo),
    .in2(in2_lo),
    .out(z0)
);
aes_gf8_mul u1_gf8_mul (
    .clk(clk),
    .reset_n(reset_n),
    .in1(in1_lo ^ in1_hi),
    .in2(in2_lo ^ in2_hi),
    .out(z1)
);
aes_gf8_mul u2_gf8_mul (
    .clk(clk),
    .reset_n(reset_n),
    .in1(in1_hi),
    .in2(in2_hi),
    .out(z2)
);

// always@(posedge clk or negedge reset_n) begin
//     if(!reset_n) begin
//         out <= 0;
//     end
//     else begin
//         out <= {z2, 16'd0} ^ {8'd0, (z0^z1^z2), 8'd0} ^ {16'd0, z0};
//     end
// end

assign out = {z2, 16'd0} ^ {8'd0, (z0^z1^z2), 8'd0} ^ {16'd0, z0};

endmodule