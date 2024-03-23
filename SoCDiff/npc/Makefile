BUILD_DIR = ./build
DIFFTEST := ./riscv64-spike-so



run: 
	$(info come npc)
	# $(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	verilator -Wno-WIDTH --top-module TopOK --cc --exe --build  csrc/*.cpp  vsrc/* socv/*.v  -LDFLAGS "-ldl" --trace
	./obj_dir/VTopOK $(IMG) ${DIFFTEST}

clean:
	-rm -rf $(BUILD_DIR)

