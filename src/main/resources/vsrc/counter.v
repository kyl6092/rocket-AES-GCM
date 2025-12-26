module CounterBlackBox #(parameter bitwidth = 8)(
    input clk,
    input rst,
    input en,
    output [bitwidth-1:0] out
);


initial begin
    $display("The passing parameter (bitwidth) is %d", bitwidth);
end

reg [bitwidth-1:0] out_reg;

assign out = out_reg;
always@(posedge clk) begin
    if (rst) begin
        out_reg <= 0;
    end
    else begin
        if (en) begin
            out_reg <= out_reg + 1;
        end
        else begin
            out_reg <= out_reg;
        end
    end
end


endmodule