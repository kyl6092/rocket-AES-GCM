module aes_gf8_mul (
    input clk,
    input reset_n,
    input [7:0] in1,
    input [7:0] in2,
    output [15:0] out
);

integer i;
reg [15:0] tmp1;
reg [15:0] tmp2 [0:8];
always@(*) begin
    tmp1 = {8'd0, in1};
    for (i = 0; i <= 8; i=i+1) begin
        tmp2[i] = 0;
    end
    for (i = 0; i < 8; i=i+1) begin
        if (in2[i]) begin
            tmp2[i+1] = tmp2[i] ^ (tmp1 << i);
        end
        else
            tmp2[i+1] = tmp2[i];
    end
end

assign out = tmp2[8];

endmodule