#include"VSocLite.h"
#include"verilated.h"
#include"verilated_vcd_c.h"
#include"common.h"
#include"ram.h"

double sc_time_stamp() {
	return 0;
}

void reset(VSocLite* dut_ptr){
	for(int i = 0; i<100; i++){
		dut_ptr->reset = 1;
		dut_ptr->clock = 0;
		dut_ptr->eval();
		dut_ptr->clock = 1;
		dut_ptr->eval();
		dut_ptr->reset = 0;
	}
}

void step(VSocLite* dut_ptr) {
	dut_ptr->clock = 0;
  dut_ptr->eval();
  dut_ptr->clock = 1;
  dut_ptr->eval();
}

int main(int argc, char** argv){
  VSocLite* dut_ptr = new VSocLite;
	init_ram("inst_ram.coe");
  VerilatedVcdC* tfp = new VerilatedVcdC;
  Verilated::traceEverOn(true);
	dut_ptr->trace(tfp, 99);
  tfp->open("./build/run.vcd");
  reset(dut_ptr);
	printf("Reset finish, start running\n\n");
  for(int i = 0; i<10; i++){
    step(dut_ptr);
		tfp->dump(i);
  }
  tfp->close();
  
  delete dut_ptr;
}
