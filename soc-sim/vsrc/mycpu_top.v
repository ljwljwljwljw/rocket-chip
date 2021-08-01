module mycpu_top(
  output [3:0]  cpu_ar_id,
  output [31:0] cpu_ar_addr,
  output [7:0]  cpu_ar_len,
  output [2:0]  cpu_ar_size,
  output [1:0]  cpu_ar_burst,
  output        cpu_ar_lock,
  output [3:0]  cpu_ar_cache,
  output [2:0]  cpu_ar_prot,
  output [3:0]  cpu_ar_qos,
  input  [3:0]  cpu_r_id,
  input  [31:0] cpu_r_data,
  input  [1:0]  cpu_r_resp,
  input         cpu_r_last,
  output [3:0]  cpu_aw_id,
  output [31:0] cpu_aw_addr,
  output [7:0]  cpu_aw_len,
  output [2:0]  cpu_aw_size,
  output [1:0]  cpu_aw_burst,
  output        cpu_aw_lock,
  output [3:0]  cpu_aw_cache,
  output [2:0]  cpu_aw_prot,
  output [3:0]  cpu_aw_qos,
  output [31:0] cpu_w_data,
  output [3:0]  cpu_w_strb,
  output        cpu_w_last,
  input  [3:0]  cpu_b_id,
  input  [1:0]  cpu_b_resp,
  output        cpu_ar_valid,
  output        cpu_aw_valid,
  output        cpu_w_valid,
  input         cpu_ar_ready,
  input         cpu_aw_ready,
  input         cpu_w_ready,
  input         cpu_r_valid,
  input         cpu_b_valid,
  output        cpu_r_ready,
  output        cpu_b_ready,
  input  [5:0]  ext_int,
  input         aclk,
  input         aresetn,
  output [31:0] debug_wb_pc,
  output        debug_wb_rf_wen,
  output [4:0]  debug_wb_rf_wnum,
  output [31:0] debug_wb_rf_wdata
);
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_0;
`endif // RANDOMIZE_REG_INIT
  wire  _T_1 = ~aresetn; // @[SocLite.scala 59:37]
  reg [9:0] REG; // @[SocLite.scala 60:22]
  wire [9:0] _T_3 = REG + 10'h1; // @[SocLite.scala 61:16]
  assign cpu_ar_id = 4'h0;
  assign cpu_ar_addr = 32'h0;
  assign cpu_ar_len = 8'h0;
  assign cpu_ar_size = 3'h0;
  assign cpu_ar_burst = 2'h0;
  assign cpu_ar_lock = 1'h0;
  assign cpu_ar_cache = 4'h0;
  assign cpu_ar_prot = 3'h0;
  assign cpu_ar_qos = 4'h0;
  assign cpu_aw_id = 4'h0;
  assign cpu_aw_addr = 32'h0;
  assign cpu_aw_len = 8'h0;
  assign cpu_aw_size = 3'h0;
  assign cpu_aw_burst = 2'h0;
  assign cpu_aw_lock = 1'h0;
  assign cpu_aw_cache = 4'h0;
  assign cpu_aw_prot = 3'h0;
  assign cpu_aw_qos = 4'h0;
  assign cpu_w_data = 32'h0;
  assign cpu_w_strb = 4'h0;
  assign cpu_w_last = 1'h0;
  assign cpu_ar_valid = 1'h0;
  assign cpu_aw_valid = 1'h0;
  assign cpu_w_valid = 1'h0;
  assign cpu_r_ready = 1'h0;
  assign cpu_b_ready = 1'h0;
  assign debug_wb_pc = 32'h0;
  assign debug_wb_rf_wen = 1'h0;
  assign debug_wb_rf_wnum = 5'h0;
  assign debug_wb_rf_wdata = 32'h0;
  always @(posedge aclk) begin
    if (_T_1) begin // @[SocLite.scala 60:22]
      REG <= 10'h0; // @[SocLite.scala 60:22]
    end else begin
      REG <= _T_3; // @[SocLite.scala 61:9]
    end
    `ifndef SYNTHESIS
    `ifdef PRINTF_COND
      if (`PRINTF_COND) begin
    `endif
        if (~_T_1) begin
          $fwrite(32'h80000002,"cnt = %d\n",REG); // @[SocLite.scala 62:11]
        end
    `ifdef PRINTF_COND
      end
    `endif
    `endif // SYNTHESIS
  end
// Register and memory initialization
`ifdef RANDOMIZE_GARBAGE_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_INVALID_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_REG_INIT
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_MEM_INIT
`define RANDOMIZE
`endif
`ifndef RANDOM
`define RANDOM $random
`endif
`ifdef RANDOMIZE_MEM_INIT
  integer initvar;
`endif
`ifndef SYNTHESIS
`ifdef FIRRTL_BEFORE_INITIAL
`FIRRTL_BEFORE_INITIAL
`endif
initial begin
  `ifdef RANDOMIZE
    `ifdef INIT_RANDOM
      `INIT_RANDOM
    `endif
    `ifndef VERILATOR
      `ifdef RANDOMIZE_DELAY
        #`RANDOMIZE_DELAY begin end
      `else
        #0.002 begin end
      `endif
    `endif
`ifdef RANDOMIZE_REG_INIT
  _RAND_0 = {1{`RANDOM}};
  REG = _RAND_0[9:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
