module aes_gf128_reduce (
    input clk,
    input reset_n,
    input [255:0] in,
    output reg [127:0] out
);

wire [127:0] in_lo, in_hi;
// reg [127:0] tmp;
assign in_lo = in[127:0];
assign in_hi = in[255:128];

integer i;
always@(*) begin
    out[0]  = in_hi[0] ^ in_hi[121] ^ in_hi[126] ^ in_lo[0];
    out[1]  = in_hi[0] ^ in_hi[1]   ^ in_hi[121] ^ in_hi[122] ^ in_hi[126] ^ in_lo[1];
    out[2]  = in_hi[0] ^ in_hi[1]   ^ in_hi[2]   ^ in_hi[121] ^ in_hi[122] ^ in_hi[123] ^ in_hi[126] ^ in_lo[2];
    out[3]  = in_hi[1] ^ in_hi[2]   ^ in_hi[3]   ^ in_hi[122] ^ in_hi[123] ^ in_hi[124] ^ in_lo[3];
    out[4]  = in_hi[2] ^ in_hi[3]   ^ in_hi[4]   ^ in_hi[123] ^ in_hi[124] ^ in_hi[125] ^ in_lo[4];
    out[5]  = in_hi[3] ^ in_hi[4]   ^ in_hi[5]   ^ in_hi[124] ^ in_hi[125] ^ in_hi[126] ^ in_lo[5];
    out[6]  = in_hi[4] ^ in_hi[5]   ^ in_hi[6]   ^ in_hi[125] ^ in_hi[126] ^ in_lo[6];
    out[7]  = in_hi[0] ^ in_hi[5]   ^ in_hi[6]   ^ in_hi[7]   ^ in_hi[121] ^ in_lo[7];
    out[8]  = in_hi[1] ^ in_hi[6]   ^ in_hi[7]   ^ in_hi[8]   ^ in_hi[122] ^ in_lo[8];
    out[9]  = in_hi[2] ^ in_hi[7]   ^ in_hi[8]   ^ in_hi[9]   ^ in_hi[123] ^ in_lo[9];
    out[10] = in_hi[3] ^ in_hi[8]   ^ in_hi[9]   ^ in_hi[10]  ^ in_hi[124] ^ in_lo[10];
    out[11] = in_hi[4] ^ in_hi[9]   ^ in_hi[10]  ^ in_hi[11]  ^ in_hi[125] ^ in_lo[11];
    out[12] = in_hi[5] ^ in_hi[10]  ^ in_hi[11]  ^ in_hi[12]  ^ in_hi[126] ^ in_lo[12];

    for(int i = 13; i < 127; i = i + 1)begin
        out[i] = in_hi[i-7] ^ in_hi[i-2] ^ in_hi[i-1] ^ in_hi[i] ^ in_lo[i];
    end

    out[127] = in_hi[120] ^ in_hi[125] ^ in_hi[126] ^ in_lo[127];
end

// always@(posedge clk or negedge reset_n) begin
//     if(!reset_n) begin
//         out <= 0;
//     end
//     else begin
//         out <= tmp;
//     end
// end

endmodule