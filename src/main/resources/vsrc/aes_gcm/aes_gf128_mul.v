module aes_gf128_mul (
    input [127:0] in1,
    input [127:0] in2,
    output reg valid,
    output [127:0] out
);


reg [127:0] z [0:128];
reg [127:0] v [0:128];
reg [127:0] r;

reg [127:0] v_lsb;

integer i;

reg [127:0] tmp;
always@(*) begin
    for (i = 0; i < 128; i=i+1) begin
        tmp = v[i];
        v_lsb[i] = tmp[0];
    end
end

always@(*) begin
    valid = 1;

    r = {8'b11100001, 120'd0};
    z[0] = 0;
    v[0] = in2;
    for (i = 0; i < 128; i=i+1) begin
        if (in1[127-i]) begin
            z[i+1] = z[i] ^ v[i];
        end
        else begin
            z[i+1] = z[i];
        end
        tmp = v[i];
        if (tmp[0])
            v[i+1] = (v[i] >> 1) ^ r;
        else
            v[i+1] = (v[i] >> 1);
    end
end

assign out = z[128];



endmodule