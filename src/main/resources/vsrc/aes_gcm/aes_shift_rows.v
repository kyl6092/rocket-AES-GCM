module aes_shift_rows (
    input [127:0] state,
    output [127:0] state_rot
);

// Row 0
assign state_rot[127:120] = state[127:120];
assign state_rot[95:88]   = state[95:88];
assign state_rot[63:56]   = state[63:56];
assign state_rot[31:24]   = state[31:24];

// Row 1

assign state_rot[119:112] = state[23:16];
assign state_rot[87:80]   = state[119:112];
assign state_rot[55:48]   = state[87:80];
assign state_rot[23:16]   = state[55:48];

// Row 2
assign state_rot[111:104] = state[47:40];
assign state_rot[79:72]   = state[15:8];
assign state_rot[47:40]   = state[111:104];
assign state_rot[15:8]    = state[79:72];

// Row 3
assign state_rot[103:96]  = state[71:64];
assign state_rot[71:64]   = state[39:32];
assign state_rot[39:32]   = state[7:0];
assign state_rot[7:0]     = state[103:96];


endmodule