#ifndef AXI4_SLAVE
#define AXI4_SLAVE

#include "axi4.hpp"

#include <queue>
#include <algorithm>
#include <utility>
#include <vector>

template <unsigned int A_WIDTH = 64, unsigned int D_WIDTH = 64,unsigned int D_WIDTH2 = 64, unsigned int ID_WIDTH = 4>
class axi4_slave {
    static_assert(D_WIDTH <= 64, "D_WIDTH should be <= 64.");
    static_assert(A_WIDTH <= 64, "A_WIDTH should be <= 64.");
    public:
        axi4_slave(int delay = 0):delay(delay) {

        }
        void beat(axi4_ref <A_WIDTH,D_WIDTH,ID_WIDTH> &pin) {
            read_channel(pin);
            write_channel(pin);
        }
        void beat2(axi4_ref <A_WIDTH,D_WIDTH2,ID_WIDTH> &pin) {
            read_channel2(pin);
            write_channel2(pin);
        }
        void reset() {
            read_busy = false;
            read_last = false;
            read_wait = false;
            read_delay = 0;
            write_busy = false;
            b_busy     = false;
            write_delay = 0;

            read_busy2 = false;
            read_last2 = false;
            read_wait2 = false;
            read_delay2 = 0;
            write_busy2 = false;
            b_busy2     = false;
            write_delay2 = 0;
        }
    protected:
        virtual axi_resp do_read (uint64_t start_addr, uint64_t size, uint8_t* buffer) = 0;
        virtual axi_resp do_write(uint64_t start_addr, uint64_t size, const uint8_t* buffer) = 0;
    private:
        unsigned int D_bytes = D_WIDTH / 8;
        unsigned int D_bytes2 = D_WIDTH2 / 8;
        int delay;
    private:
        bool read_busy = false; // during trascation except last
        bool read_last = false; // wait rready and free
        bool read_wait = false; // ar ready, but waiting the last read to ready
        int  read_delay = 0; // delay
        uint64_t        r_start_addr;   // lower bound of transaction address
        uint64_t        r_current_addr; // current burst address in r_data buffer (physical address % 4096)
        AUTO_SIG(       arid        ,ID_WIDTH-1,0);
        axi_burst_type  r_burst_type;
        unsigned int    r_each_len;
        unsigned int    r_nr_trans;
        unsigned int    r_cur_trans;
        unsigned int    r_tot_len;
        bool            r_out_ready;
        bool            r_early_err;
        axi_resp        r_resp;
        // uint8_t         r_data[4096];
        uint8_t         r_data[8192];

        bool read_check() {
            if (r_burst_type == BURST_RESERVED) return false;
            if (r_burst_type == BURST_WRAP && (r_current_addr % r_each_len)) return false;
            if (r_burst_type == BURST_WRAP) {
                if (r_nr_trans != 2 || r_nr_trans != 4 || r_nr_trans != 8 || r_nr_trans != 16) {
                    return false;
                }
            }
            uint64_t rem_addr = 8192 - (r_start_addr % 4096);//??
            if (r_tot_len > rem_addr) return false;
            if (r_each_len > D_bytes) return false;
            return true;
        }

        void read_beat(axi4_ref <A_WIDTH,D_WIDTH,ID_WIDTH> &pin) {
            pin.rid = arid;
            pin.rvalid  = 1;
            bool update = false;
            if (pin.rready || r_cur_trans == 0) {
                r_cur_trans += 1;
                update = true;
                if (r_cur_trans == r_nr_trans) {
                    read_last = true;
                    read_busy = false;
                }
            }
            pin.rlast = read_last;
            if (update) {
                if (r_early_err) {
                    pin.rresp = RESP_DECERR;
                    pin.rdata = 0;
                }
                // else if (r_burst_type == BURST_FIXED) {
                //     pin.rresp = do_read(static_cast<uint64_t>(r_start_addr), static_cast<uint64_t>(r_tot_len), &r_data[r_start_addr % 4096]);
                //     pin.rdata = *(AUTO_SIG(*,D_WIDTH-1,0))(&r_data[(r_start_addr % 4096) - (r_start_addr % D_bytes)]);
                // }
                else { // INCR, WRAP
                    pin.rresp = r_resp;
                    pin.rdata = *(AUTO_SIG(*,D_WIDTH-1,0))(&r_data[r_current_addr - (r_current_addr % D_bytes)]);
                    r_current_addr += r_each_len - (r_current_addr % r_each_len);
                    // if (r_burst_type == BURST_WRAP && r_current_addr == ((r_start_addr % 4096) + r_each_len * r_nr_trans)) {
                    //     r_current_addr = r_start_addr % 4096;
                    // }
                }
            }
        }

        void read_init(axi4_ref <A_WIDTH,D_WIDTH,ID_WIDTH> &pin) {
            arid            = static_cast<unsigned int>(pin.arid);
            r_burst_type    = static_cast<axi_burst_type>(pin.arburst);
            r_each_len      = 1 << pin.arsize;
            r_nr_trans      = pin.arlen + 1;
            r_current_addr  = (r_burst_type == BURST_WRAP) ? (pin.araddr % 4096) : ((pin.araddr % 4096) - (pin.araddr % r_each_len));
            r_start_addr    = (r_burst_type == BURST_WRAP) ? (pin.araddr - (pin.araddr % (r_each_len * r_nr_trans) ) ) : pin.araddr;
            r_cur_trans     = 0;
            r_tot_len       = ( (r_burst_type == BURST_FIXED) ? r_each_len : r_each_len * r_nr_trans) - (r_start_addr % r_each_len); // first beat can be unaligned
            r_early_err     = !read_check();
            // printf("r_burst_type %d\n",r_burst_type);
            // printf("r_tot_len %d  ",r_tot_len);
            //     printf("rem_addr %d\n",4096 - (r_start_addr % 4096));
            //     printf("r_start_addr %x  ",r_start_addr);
            //     printf("pin.awaddr %x\n\n",pin.awaddr);
            assert(!r_early_err);
            // clear unused bits.
            if (r_start_addr % D_bytes) {
                uint64_t clear_addr = r_start_addr % 4096;
                clear_addr -= clear_addr % D_bytes;
                memset(&r_data[clear_addr],0x00,D_bytes);
            }
            if ((r_start_addr + r_tot_len) % D_bytes) {
                uint64_t clear_addr = (r_start_addr + r_tot_len) % 4096;
                clear_addr -= (clear_addr % D_bytes);
                memset(&r_data[clear_addr],0x00,D_bytes);
            }
            // For BURST_FIXED, we call do_read every read burst
            if (!r_early_err && r_burst_type != BURST_FIXED) 
                r_resp = do_read(static_cast<uint64_t>(r_start_addr), static_cast<uint64_t>(r_tot_len), &r_data[r_start_addr % 4096] );
        }

        void read_channel(axi4_ref <A_WIDTH,D_WIDTH,ID_WIDTH> &pin) {
            // Read step 1. release old transaction
            if (read_last && pin.rready) {
                read_last = false;
                pin.rvalid = 0;     // maybe change in the following code
                pin.rlast = 0;
                if (read_wait) {
                    read_wait = false;
                    read_busy = true;
                    read_delay = delay;
                }
            }
            // Read step 2. check new address come
            if (pin.arready && pin.arvalid) {
                read_init(pin);
                if (read_last) read_wait = true;
                else {
                    read_busy = true;
                    read_delay = delay;
                }
            }
            // Read step 3. do read trascation
            if (read_busy) {
                if (read_delay) read_delay --;
                else read_beat(pin);
            }
            // Read step 4. set arready before new address come, it will change read_busy and read_wait status
            pin.arready = !read_busy && !read_wait;
        }
    private:
        bool write_busy = false;
        bool b_busy     = false;
        int  write_delay = 0;
        uint64_t        w_start_addr;
        uint64_t        w_current_addr;
        AUTO_SIG(       awid        ,ID_WIDTH-1,0);
        axi_burst_type  w_burst_type;
        unsigned int    w_each_len;
        int             w_nr_trans;
        int             w_cur_trans;
        unsigned int    w_tot_len;
        bool            w_out_ready;
        bool            w_early_err;
        axi_resp        w_resp;
        uint8_t         w_buffer[D_WIDTH/8];
        bool write_check() {
            if (w_burst_type == BURST_RESERVED || w_burst_type == BURST_FIXED) return false;
            if (w_burst_type == BURST_WRAP && (w_current_addr % w_each_len)) return false;
            if (w_burst_type == BURST_WRAP) {
                if (w_nr_trans != 2 || w_nr_trans != 4 || w_nr_trans != 8 || w_nr_trans != 16) return false;
            }
            // uint64_t rem_addr = 4096 - (w_start_addr % 4096);
            // if (w_tot_len > rem_addr) return false;pigfly
            if (w_each_len > D_bytes) return false;
            return true;
        }
        void write_init(axi4_ref <A_WIDTH,D_WIDTH,ID_WIDTH> &pin) {
            awid            = pin.awid;
            w_burst_type    = static_cast<axi_burst_type>(pin.awburst);
            w_each_len      = 1 << pin.awsize;
            w_nr_trans      = pin.awlen + 1;
            w_current_addr  = (w_burst_type == BURST_WRAP) ? pin.awaddr : (pin.awaddr - (pin.awaddr % w_each_len));
            w_start_addr    = (w_burst_type == BURST_WRAP) ? (pin.awaddr - (pin.awaddr % (w_each_len * w_nr_trans))) : pin.awaddr;
            w_cur_trans     = 0;
            w_tot_len       = w_each_len * w_nr_trans - (w_start_addr % w_each_len);
            w_early_err     = !write_check();
            assert(!w_early_err);
            w_resp          = RESP_OKEY;
        }
        // pair<start,len>
        std::vector<std::pair<int,int> > strb_to_range(AUTO_IN (wstrb,(D_WIDTH/8)-1, 0), int st_pos, int ed_pos) {
            std::vector<std::pair<int,int> > res;
            int l = st_pos;
            while (l < ed_pos) {
                if ((wstrb >> l) & 1) {
                    int r = l;
                    while ((wstrb >> r) & 1 && r < ed_pos) r ++;
                    res.emplace_back(l,r-l);
                    l = r + 1;
                }
                else l ++;
            }
            return res;
        }
        void write_beat(axi4_ref <A_WIDTH,D_WIDTH,ID_WIDTH> &pin) {
            if (pin.wvalid && pin.wready) {
                w_cur_trans += 1;
                if (w_cur_trans == w_nr_trans) {
                    write_busy = false;
                    b_busy = true;
                    if (!pin.wlast) {
                        w_early_err = true;
                        // assert(false);
                    }
                }
                if (w_early_err) return;
                uint64_t addr_base = w_current_addr;
                w_current_addr += w_each_len - (addr_base % w_each_len);
                if (w_current_addr == (w_start_addr + w_each_len * w_nr_trans)) w_cur_trans =  w_start_addr; // warp support
                uint64_t in_data_pos = addr_base % D_bytes;
                addr_base -= addr_base % D_bytes;
                uint64_t rem_data_pos = w_each_len - (in_data_pos % w_each_len); // unaligned support
                // split discontinuous wstrb bits to small requests
                std::vector<std::pair<int,int> > range = strb_to_range(pin.wstrb,in_data_pos,in_data_pos+rem_data_pos);
                for (std::pair<int,int> sub_range : range) {
                    int &addr = sub_range.first;
                    int &len  = sub_range.second;
                    memcpy(w_buffer,&(pin.wdata),sizeof(pin.wdata));
                    w_resp = static_cast<axi_resp>(static_cast<int>(w_resp) | static_cast<int>(do_write(addr_base+addr,len,w_buffer+addr)));
                }
            }
        }
        void b_beat(axi4_ref <A_WIDTH,D_WIDTH,ID_WIDTH> &pin) {
            pin.bid = awid;
            pin.bresp = w_early_err ? RESP_DECERR : w_resp;
            if (pin.bvalid && pin.bready) b_busy = false;
        }
        void write_channel(axi4_ref <A_WIDTH,D_WIDTH,ID_WIDTH> &pin) {
            if (pin.awready && pin.awvalid) {
                write_init(pin);
                write_busy = true;
                write_delay = delay;
            }
            if (write_busy) {
                if (write_delay) write_delay --;
                else write_beat(pin);
            }
            if (b_busy) {
                b_beat(pin);
            }
            pin.bvalid = b_busy;
            pin.awready = !write_busy && !b_busy;
            if (delay) pin.wready = write_busy && !write_delay;
            else pin.wready = !b_busy;
        }

    private:
        bool read_busy2 = false; // during trascation except last
        bool read_last2 = false; // wait rready and free
        bool read_wait2 = false; // ar ready, but waiting the last read to ready
        int  read_delay2 = 0; // delay
        uint64_t        r_start_addr2;   // lower bound of transaction address
        uint64_t        r_current_addr2; // current burst address in r_data buffer (physical address % 4096)
        AUTO_SIG(       arid2        ,ID_WIDTH-1,0);
        axi_burst_type  r_burst_type2;
        unsigned int    r_each_len2;
        unsigned int    r_nr_trans2;
        unsigned int    r_cur_trans2;
        unsigned int    r_tot_len2;
        bool            r_out_ready2;
        bool            r_early_err2;
        axi_resp        r_resp2;
        // uint8_t         r_data[4096];
        uint8_t         r_data2[8192*2];

        bool read_check2() {
            // if (r_burst_type2 == BURST_RESERVED) return false;
            // if (r_burst_type == BURST_WRAP && (r_current_addr % r_each_len)) return false;
            // if (r_burst_type == BURST_WRAP) {
            //     if (r_nr_trans != 2 || r_nr_trans != 4 || r_nr_trans != 8 || r_nr_trans != 16) {
            //         return false;
            //     }
            // }
            uint64_t rem_addr2 = 8192 - (r_start_addr2 % 4096);//??
            if (r_tot_len2 > rem_addr2) return false;
            if (r_each_len2 > D_bytes2) return false;
            return true;
        }

        void read_beat2(axi4_ref <A_WIDTH,D_WIDTH2,ID_WIDTH> &pin) {
            pin.rid = arid2;
            pin.rvalid  = 1;
            bool update = false;
            if (pin.rready || r_cur_trans2 == 0) {
                r_cur_trans2 += 1;
                update = true;
                if (r_cur_trans2 == r_nr_trans2) {
                    read_last2 = true;
                    read_busy2 = false;
                }
            }
            pin.rlast = read_last2;
            if (update) {
                if (r_early_err2) {
                    pin.rresp = RESP_DECERR;
                    for(int i =0 ; i < 8 ; i++ )
                     pin.rdata[i] = 0;
                }
                // else if (r_burst_type == BURST_FIXED) {
                //     pin.rresp = do_read(static_cast<uint64_t>(r_start_addr), static_cast<uint64_t>(r_tot_len), &r_data[r_start_addr % 4096]);
                //     pin.rdata = *(AUTO_SIG(*,D_WIDTH-1,0))(&r_data[(r_start_addr % 4096) - (r_start_addr % D_bytes)]);
                // }
                else { // INCR, WRAP
                    pin.rresp = r_resp2;
                    for(int i =0 ; i < 8 ; i++ )
                    pin.rdata[i] = (*(AUTO_SIG(*,D_WIDTH2-1,0))(&r_data2[r_current_addr2 - (r_current_addr2 % D_bytes2)]))[i];
                    r_current_addr2 += r_each_len2 - (r_current_addr2 % r_each_len2);
                    // if (r_burst_type == BURST_WRAP && r_current_addr == ((r_start_addr % 4096) + r_each_len * r_nr_trans)) {
                    //     r_current_addr = r_start_addr % 4096;
                    // }
                }
            }
        }

        void read_init2(axi4_ref <A_WIDTH,D_WIDTH2,ID_WIDTH> &pin) {
            arid2            = static_cast<unsigned int>(pin.arid);
            r_burst_type2    = static_cast<axi_burst_type>(pin.arburst);
            r_each_len2      = 1 << pin.arsize;
            r_nr_trans2      = pin.arlen + 1;
            r_current_addr2  = (r_burst_type2 == BURST_WRAP) ? (pin.araddr % 4096) : ((pin.araddr % 4096) - (pin.araddr % r_each_len2));
            r_start_addr2    = (r_burst_type2 == BURST_WRAP) ? (pin.araddr - (pin.araddr % (r_each_len2 * r_nr_trans2) ) ) : pin.araddr;
            r_cur_trans2     = 0;
            r_tot_len2       = ( (r_burst_type2 == BURST_FIXED) ? r_each_len2 : r_each_len2 * r_nr_trans2) - (r_start_addr2 % r_each_len2); // first beat can be unaligned
            r_early_err2     = !read_check();
            // printf("r_burst_type %d\n",r_burst_type);
            // printf("r_tot_len %d  ",r_tot_len);
            //     printf("rem_addr %d\n",4096 - (r_start_addr % 4096));
            //     printf("r_start_addr %x  ",r_start_addr);
            //     printf("pin.awaddr %x\n\n",pin.awaddr);
            assert(!r_early_err2);
            // clear unused bits.
            if (r_start_addr2 % D_bytes2) {
                uint64_t clear_addr = r_start_addr2 % 4096;
                clear_addr -= clear_addr % D_bytes2;
                memset(&r_data2[clear_addr],0x00,D_bytes2);
            }
            if ((r_start_addr2 + r_tot_len2) % D_bytes2) {
                uint64_t clear_addr = (r_start_addr2 + r_tot_len2) % 4096;
                clear_addr -= (clear_addr % D_bytes2);
                memset(&r_data2[clear_addr],0x00,D_bytes2);
            }
            // For BURST_FIXED, we call do_read every read burst
            if (!r_early_err2 && r_burst_type2 != BURST_FIXED) 
                r_resp2 = do_read(static_cast<uint64_t>(r_start_addr2), static_cast<uint64_t>(r_tot_len2), &r_data2[r_start_addr2 % 4096] );
        }

        void read_channel2(axi4_ref <A_WIDTH,D_WIDTH2,ID_WIDTH> &pin) {
            // Read step 1. release old transaction
            if (read_last2 && pin.rready) {
                read_last2 = false;
                pin.rvalid = 0;     // maybe change in the following code
                pin.rlast = 0;
                if (read_wait2) {
                    read_wait2 = false;
                    read_busy2 = true;
                    read_delay2 = delay;
                }
            }
            // Read step 2. check new address come
            if (pin.arready && pin.arvalid) {
                read_init2(pin);
                if (read_last2) read_wait2 = true;
                else {
                    read_busy2 = true;
                    read_delay2 = delay;
                }
            }
            // Read step 3. do read trascation
            if (read_busy2) {
                if (read_delay2) read_delay2 --;
                else read_beat2(pin);
            }
            // Read step 4. set arready before new address come, it will change read_busy and read_wait status
            pin.arready = !read_busy2 && !read_wait2;
        }

    private:
        bool write_busy2 = false;
        bool b_busy2     = false;
        int  write_delay2 = 0;
        uint64_t        w_start_addr2;
        uint64_t        w_current_addr2;
        AUTO_SIG(       awid2        ,ID_WIDTH-1,0);
        axi_burst_type  w_burst_type2;
        unsigned int    w_each_len2;
        int             w_nr_trans2;
        int             w_cur_trans2;
        unsigned int    w_tot_len2;
        bool            w_out_ready2;
        bool            w_early_err2;
        axi_resp        w_resp2;
        uint8_t         w_buffer2[D_WIDTH2/8];
        bool write_check2() {
            // uint64_t rem_addr = 4096 - (w_start_addr % 4096);
            // if (w_tot_len > rem_addr) return false;pigfly
            if (w_each_len2 > D_bytes2) return false;
            return true;
        }
        void write_init2(axi4_ref <A_WIDTH,D_WIDTH2,ID_WIDTH> &pin) {
            awid2            = pin.awid;
            w_burst_type2    = static_cast<axi_burst_type>(pin.awburst);
            w_each_len2      = 1 << pin.awsize;
            w_nr_trans2      = pin.awlen + 1;
            w_current_addr2  = (w_burst_type2 == BURST_WRAP) ? pin.awaddr : (pin.awaddr - (pin.awaddr % w_each_len2));
            w_start_addr2    = (w_burst_type2 == BURST_WRAP) ? (pin.awaddr - (pin.awaddr % (w_each_len2 * w_nr_trans2))) : pin.awaddr;
            w_cur_trans2     = 0;
            w_tot_len2       = w_each_len2 * w_nr_trans2 - (w_start_addr2 % w_each_len2);
            w_early_err2     = !write_check2();
            assert(!w_early_err2);
            w_resp2          = RESP_OKEY;
        }
        // pair<start,len>
        std::vector<std::pair<int,int> > strb_to_range2(AUTO_IN (wstrb,(D_WIDTH2/8)-1, 0), int st_pos, int ed_pos) {
            std::vector<std::pair<int,int> > res;
            int l = st_pos;
            while (l < ed_pos) {
                if ((wstrb >> l) & 1) {
                    int r = l;
                    while ((wstrb >> r) & 1 && r < ed_pos) r ++;
                    res.emplace_back(l,r-l);
                    l = r + 1;
                }
                else l ++;
            }
            return res;
        }
        void write_beat2(axi4_ref <A_WIDTH,D_WIDTH2,ID_WIDTH> &pin) {
            if (pin.wvalid && pin.wready) {
                w_cur_trans2 += 1;
                if (w_cur_trans2 == w_nr_trans2) {
                    write_busy2 = false;
                    b_busy2 = true;
                    if (!pin.wlast) {
                        w_early_err2 = true;
                        // assert(false);
                    }
                }
                if (w_early_err2) return;
                uint64_t addr_base2 = w_current_addr2;
                w_current_addr2 += w_each_len2 - (addr_base2 % w_each_len2);
                if (w_current_addr2 == (w_start_addr2 + w_each_len2 * w_nr_trans2)) w_cur_trans2 =  w_start_addr2; // warp support
                uint64_t in_data_pos2 = addr_base2 % D_bytes2;
                addr_base2 -= addr_base2 % D_bytes2;
                uint64_t rem_data_pos2 = w_each_len2 - (in_data_pos2 % w_each_len2); // unaligned support
                // split discontinuous wstrb bits to small requests
                std::vector<std::pair<int,int> > range2 = strb_to_range2(pin.wstrb,in_data_pos2,in_data_pos2 + rem_data_pos2);
                for (std::pair<int,int> sub_range : range2) {
                    int &addr = sub_range.first;
                    int &len  = sub_range.second;
                    memcpy(w_buffer2,&(pin.wdata),sizeof(pin.wdata));
                    w_resp2 = static_cast<axi_resp>(static_cast<int>(w_resp2) | static_cast<int>(do_write(addr_base2+addr,len,w_buffer2+addr)));
                }
            }
        }
        void b_beat2(axi4_ref <A_WIDTH,D_WIDTH2,ID_WIDTH> &pin) {
            pin.bid = awid2;
            pin.bresp = w_early_err2 ? RESP_DECERR : w_resp2;
            if (pin.bvalid && pin.bready) b_busy2 = false;
        }
        void write_channel2(axi4_ref <A_WIDTH,D_WIDTH2,ID_WIDTH> &pin) {
            if (pin.awready && pin.awvalid) {
                write_init2(pin);
                write_busy2 = true;
                write_delay2 = delay;
            }
            if (write_busy2) {
                if (write_delay2) write_delay2 --;
                else write_beat2(pin);
            }
            if (b_busy2) {
                b_beat2(pin);
            }
            pin.bvalid = b_busy2;
            pin.awready = !write_busy2 && !b_busy2;
            if (delay) pin.wready = write_busy2 && !write_delay2;
            else pin.wready = !b_busy2;
        }
};

#endif
