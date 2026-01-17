module fifo_mini(
    input clk,
    input reset_n,
    input push,
    input [127:0] datain,
    input pop,
    output reg [127:0] dataout
);

reg [127:0] mem [0:31];
reg [4:0] front_ptr;
reg [4:0] rear_ptr;


always@(posedge clk) begin: fifo_content
    if (push) begin
        mem[front_ptr] <= datain;
    end
    if (pop) begin
        dataout <= mem[rear_ptr];
    end
end

always@(posedge clk or negedge reset_n) begin
    if (!reset_n) begin
        front_ptr <= 0;
        rear_ptr <= 0;
    end
    else begin
        if (push) begin
            front_ptr <= front_ptr + 1;
        end
        if (pop) begin
            rear_ptr <= rear_ptr + 1;
        end
    end
end

endmodule