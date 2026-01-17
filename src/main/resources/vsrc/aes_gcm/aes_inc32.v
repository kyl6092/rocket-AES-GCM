module aes_inc32 (
    input [127:0] in,
    output [127:0] out
);

wire [31:0] in32_plus_one; 
assign in32_plus_one = in[127:96] + 32'd1;
assign out = {in32_plus_one, in[95:0]};


endmodule