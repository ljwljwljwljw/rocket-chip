BUILD_DIR ?= $(abspath ./build)
TOP = SocLite
TOP_V = $(BUILD_DIR)/$(TOP).v

VSRC = $(abspath $(shell find ./vsrc -name "*.v"))
CSRC = $(abspath $(shell find ./csrc -name "*.cpp"))
SRCS = $(TOP_V) $(VSRC) $(CSRC)

VERILATOR_FLAGS =				        \
	+define+VERILATOR=1					\
	+define+PRINTF_COND=1				\
	+define+RANDOMIZE_REG_INIT        	\
	+define+RANDOMIZE_MEM_INIT        	\
	+define+RANDOMIZE_GARBAGE_ASSIGN  	\
	+define+RANDOMIZE_DELAY=0			\
	--trace								\
	--assert

EMU_MK = $(BUILD_DIR)/V$(TOP).mk
EMU = $(BUILD_DIR)/$(TOP)

.DEFAULT_GOAL = run

SCALA_SRC = $(shell find ../src/main/scala/soclite -name "*.scala")

$(TOP_V): $(SCALA_SRC)
	@echo Generating SocLite ...
	cd .. && sbt 'runMain soclite.SocLite -td $(BUILD_DIR)'

$(EMU_MK): $(SRCS)
	verilator $(VERILATOR_FLAGS) -cc -exe $^ --top-module $(TOP) -o $(TOP) -Mdir $(BUILD_DIR)

$(EMU): $(EMU_MK) $(SRCS)
	$(MAKE) -C $(@D) -f $(<F) VM_PARALLEL_BUILDS=1 OPT_FAST="-O3"

run: $(EMU) $(SRCS)
	@echo Start running emulator:[$(abspath $<)]
	@$<

clean:
	rm -rf $(BUILD_DIR)

.PHONY: run clean