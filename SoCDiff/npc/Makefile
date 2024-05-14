BUILD_DIR = ./build
DIFFTEST := ./riscv64-spike-so

watch Verilated::t_s.t_dpiScopep

run: 
	$(info come npc)
	# $(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	# verilator -Wno-WIDTH --top-module TopOK --cc --exe --build  csrc/*.cpp  vsrc/* socv/*.v  -LDFLAGS "-lSDL2 -ldl" --trace
	verilator -OO -CFLAGS -O0 -CFLAGS -ggdb -LDFLAGS -O0 -LDFLAGS -ggdb -CFLAGS -DVL_DEBUG=1 -CFLAGS -D_GLIBCXX_DEBUG -Wno-WIDTH --top-module TopOK --cc --exe --build  csrc/*.cpp  vsrc/* socv/*.v  -LDFLAGS "-lSDL2 -ldl" --trace
	./obj_dir/VTopOK $(IMG) ${DIFFTEST}

clean:
	-rm -rf $(BUILD_DIR)

.PHONY: test verilog help compile bsp reformat checkformat clean

# sim:	verilog
# 	# verilator -Wall --cc --exe --build csrc/sim_main.cpp vsrc/top.v --trace
# 	verilator -Wall -Wno-DECLFILENAME -Wno-EOFNEWLINE -Wno-UNUSED --top-module riscv --cc --exe --build  csrc/riscvtest.cpp csrc/pmem.cpp $(BUILD_DIR)/riscv.v  $(BUILD_DIR)/ebProbe.v 
# 	obj_dir/Vriscv
# 	# gtkwave wave.vcd
# 	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
# 	@echo "Write this Makefile by yourself."
gdb:
	verilator  -OO -CFLAGS -O0 -CFLAGS -ggdb -LDFLAGS -O0 -LDFLAGS -ggdb -CFLAGS -DVL_DEBUG=1 -CFLAGS -D_GLIBCXX_DEBUG -Wno-WIDTH --top-module TopOK --cc --exe --build  csrc/*.cpp  vsrc/* socv/*.v  -LDFLAGS "-lSDL2 -ldl" --trace
# test:
# 	verilator -Wno-WIDTH --top-module riscv --cc --exe --build  csrc/riscvtest.cpp csrc/pmem.cpp  csrc/difftest.cpp csrc/keyboard.cpp csrc/vga.cpp vsrc/riscv.v  playground/src/main/vsrc/*  -LDFLAGS "-lSDL2 -ldl"

# include ../Makefile
